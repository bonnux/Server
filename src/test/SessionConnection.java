package test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SessionConnection {
	private Socket mClient;
	private SessionConnection.ReadThread mReadThread;
	private SessionConnection.WriteThread mWriteThread;
	private InputStream inputStream;
	private OutputStream outputStream;
	private List<byte[]> mListSend;
	private int i = 0;
	private SessionConnection.SessionListener mSessionListener;
	private long mlLastRcvDataTime = System.currentTimeMillis();
	private long mlLastSendDataTime = System.currentTimeMillis();

	//对应manager，manager实际是一个线程
	public interface SessionListener {
		void addSessionConnection(SessionConnection var1);

		void removeSessionConnection(SessionConnection var1);
	}

	//建立连接
	public SessionConnection(Socket mClient, SessionConnection.SessionListener mSessionListener) {
		this.mClient = mClient;
		this.mSessionListener = mSessionListener;

		try {
			this.inputStream = mClient.getInputStream();
			this.outputStream = mClient.getOutputStream();
		} catch (IOException var4) {
			var4.printStackTrace();
		}

		this.mListSend = new ArrayList();
	}


	//启动连接，和其中的读写线程
	public void registerSession() {
		this.mReadThread = new SessionConnection.ReadThread();
		this.mReadThread.start();
		this.mWriteThread = new SessionConnection.WriteThread();
		this.mWriteThread.start();
		System.out.println("registerSession");
		this.mSessionListener.addSessionConnection(this);
	}

	//断开连接
	public void releaseConnection() {
		this.mReadThread.interrupt();
		this.mWriteThread.interrupt();

		try {
			this.inputStream.close();
			this.outputStream.close();
			this.mClient.close();
			this.mSessionListener.removeSessionConnection(this);
			System.out.println("客户端:" + this.mClient + "断开连接:");
		} catch (IOException var2) {
			var2.printStackTrace();
		}

	}

	//读线程
	private class ReadThread extends Thread {
		private ReadThread() {
		}

		public void run() {
			String str = "";
			boolean var2 = false;

			while(!this.isInterrupted()) {
				try {
					byte[] buffer = new byte[2048];
					int n = SessionConnection.this.inputStream.read(buffer);
					str = new String(buffer, 0, n);
					SessionConnection.this.mlLastRcvDataTime = System.currentTimeMillis();
					if (str.equals("xtb")) {
						System.out.println("客户端:" + SessionConnection.this.mClient + " 心跳包-->：" + str);
					}
				} catch (IOException var5) {
					var5.printStackTrace();
					SessionConnection.this.releaseConnection();
					break;
				}
			}

		}
	}

	//写线程
	private class WriteThread extends Thread {
		private WriteThread() {
		}

		public void run() {
			while(true) {
				if (!this.isInterrupted()) {
					try {
						Iterator var2 = SessionConnection.this.mListSend.iterator();

						while(true) {
							if (!var2.hasNext()) {
								SessionConnection.this.mListSend.clear();
								SessionConnection.this.sendXT();
								break;
							}

							byte[] data = (byte[])var2.next();
							new String(data);
							SessionConnection.this.outputStream.write(data);
						}
					} catch (IOException var5) {
						var5.printStackTrace();
						SessionConnection.this.releaseConnection();
						return;
					}

					try {
						Thread.sleep(5000L);
						continue;
					} catch (InterruptedException var4) {

					}
				}

				return;
			}
		}
	}

	//获取客户端信息
	public Socket getmClient() {
		return this.mClient;
	}

	public void setmClient(Socket mClient) {
		this.mClient = mClient;
	}

	//超时的话，断开连接
	public boolean tryToReleaseConnect2TimeOut() {
		boolean bRet = false;
		boolean bTimeOut = Math.abs(System.currentTimeMillis() - this.mlLastRcvDataTime) > 18000L;
		if (bTimeOut) {
			bRet = true;
		}

		return bRet;
	}

	//发送字符串
	public void sendDate(String obj) {
		byte[] buf = obj.getBytes();
		this.mListSend.add(buf);
	}

	//发送心跳包
	public void sendXT() {
		long TimeOut = Math.abs(System.currentTimeMillis() - this.mlLastSendDataTime);
		if (TimeOut >= 6000L) {
			this.mlLastSendDataTime = System.currentTimeMillis();
			this.sendDate("xtb");
		}

	}

}
