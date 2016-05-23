package cn.com.cardinfo.forward.session;

import java.util.Queue;

import cn.com.cardinfo.forward.event.EventSource;
/**
 * 
 * @author Zale
 *
 */
public abstract class AbstractTcpSession extends EventSource implements Session{
	
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
