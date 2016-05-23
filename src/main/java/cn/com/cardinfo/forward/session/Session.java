package cn.com.cardinfo.forward.session;

import java.util.Queue;
/**
 * 
 * @author Zale
 *
 */
public interface Session {
	public void process();
	public boolean isClosed();
	void close();
	public Queue<byte[]> getQueue();
}
