package test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import test.SessionConnection.SessionListener;

public class SessionManager extends Thread implements SessionListener {
	private List<SessionConnection> mClientList = new ArrayList();

	public SessionManager() {
	}

	//添加新的线程
	public synchronized void addSessionConnection(SessionConnection mSessionConnection) {
		this.mClientList.add(mSessionConnection);
		Iterator var3 = this.mClientList.iterator();

		while(var3.hasNext()) {
			SessionConnection c = (SessionConnection)var3.next();
			System.out.println("已经接入的socket：" + c.getmClient());
			c.sendDate("大家好我是：" + mSessionConnection.getmClient());
		}

	}

	//撤销线程
	public synchronized void removeSessionConnection(SessionConnection mSessionConnection) {
		boolean isExit = false;
		Iterator var4 = this.mClientList.iterator();

		while(var4.hasNext()) {
			SessionConnection c = (SessionConnection)var4.next();
			if (c == mSessionConnection) {
				isExit = true;
				break;
			}
		}

		if (isExit) {
			this.mClientList.remove(mSessionConnection);
		}

	}

	//
	public void run() {
		while(true) {
			if (!this.isInterrupted()) {
				SessionConnection delClient = null;
				Iterator var3 = this.mClientList.iterator();

				while(var3.hasNext()) {
					SessionConnection c = (SessionConnection)var3.next();
					if (c == null || c.tryToReleaseConnect2TimeOut()) {
						c.releaseConnection();
						delClient = c;
						break;
					}
				}
				if (delClient != null) {
					this.mClientList.remove(delClient);
					delClient = null;
					continue;
				}
				try {
					Thread.sleep(1000L);
					continue;
				} catch (InterruptedException var4) {

				}
			}
			return;
		}
	}
}