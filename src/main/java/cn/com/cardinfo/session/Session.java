package cn.com.cardinfo.session;

import java.util.Queue;

public interface Session {
	public void process();
	public boolean isClosed();
	void close();
	public Queue<byte[]> getQueue();
}
