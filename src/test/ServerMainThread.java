package test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMainThread extends Thread {
	ServerSocket mServerSocket;
	SessionManager mSessionManager;

	public ServerMainThread() {
	}

	public void run() {
		try {
			this.mServerSocket = new ServerSocket(8001);
			//开启服务端线程
			this.mSessionManager = new SessionManager();
			this.mSessionManager.start();

			System.out.println("服务器端------监听中.....");

			while(true) {
				Socket socket = this.mServerSocket.accept();
				System.out.println("接入的socket：" + socket);

				//建立连接，使得可以读写
				SessionConnection mSessionConnection = new SessionConnection(socket, this.mSessionManager);
				mSessionConnection.registerSession();
			}
		} catch (IOException var3) {
			var3.printStackTrace();
		}
	}

	public static void main(String[] args) {
		ServerMainThread mMainThread = new ServerMainThread();
		mMainThread.start();
	}
}