package cn.com.cardinfo.session;

import java.util.Queue;

public abstract class AbstractTcpSession implements Session{
	
	public abstract Queue<byte[]> getQueue();
	public abstract void setQueue(Queue<byte[]> queue);
	@Override
	public void close() {
		if(getQueue()!=null){
			getQueue().clear();
		}
		setQueue(null);
	}
	
	
}
