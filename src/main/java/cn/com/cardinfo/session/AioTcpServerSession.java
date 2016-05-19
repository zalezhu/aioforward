package cn.com.cardinfo.session;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.joda.time.Interval;

import cn.com.cardinfo.event.Event;
import cn.com.cardinfo.event.EventPublish;
import cn.com.cardinfo.event.EventType;
import cn.com.cardinfo.server.AioReadHandler;
import cn.com.cardinfo.server.MsgConsumer;
import cn.com.cardinfo.util.ExecutorsPool;

public class AioTcpServerSession extends AbstractTcpSession {
	private static Logger logger = Logger.getLogger(AioTcpServerSession.class);
	private ByteBuffer readBuffer;
	private AtomicBoolean isStarted;
	private AsynchronousSocketChannel socketChannel;
	private AsynchronousServerSocketChannel serverSocketChannel;
	private AioReadHandler readHandler;
	private int bufferSize;
	private String serverName;
	private MsgConsumer consumer;
	private  Queue<byte[]> queue;
	public Queue<byte[]> getQueue() {
		return queue;
	}

	public void setQueue(Queue<byte[]> queue) {
		this.queue = queue;
	}

	public AioTcpServerSession(AsynchronousServerSocketChannel serverSocketChannel,AsynchronousSocketChannel socketChannel, int bufferSize,
			String serverName) {
		this.socketChannel = socketChannel;
		this.serverSocketChannel = serverSocketChannel;
		this.bufferSize = bufferSize;
		this.serverName = serverName;
		initDefault();

	}

	private void initDefault() {
		if (readBuffer == null) {
			readBuffer = ByteBuffer.allocate(bufferSize);
		}
		if(queue==null){
			this.queue = new ConcurrentLinkedQueue<byte[]>();
		}
		if (isStarted == null) {
			isStarted = new AtomicBoolean(true);
		}
		if (readHandler == null) {
			readHandler = new AioReadHandler(socketChannel, serverName);
		}
		consumer = new MsgConsumer(serverName,getQueue());
		ExecutorsPool.FIXED_EXECUTORS.execute(consumer);
	}
	public ByteBuffer getReadBuffer() {
		return readBuffer;
	}


	public boolean isStarted() {
		return isStarted.get();
	}

	public void setIsStarted(boolean isStarted) {
		this.isStarted.set(isStarted);
	}

	public void read() {
		if (isStarted() && this.socketChannel.isOpen()) {
			this.socketChannel.read(this.readBuffer, this, this.readHandler);
		} else {
			throw new IllegalStateException("Session Or Channel has been closed");
		}
	}

	public void process() {
		read();
	}

	@Override
	public void close() {
		try {
			if(isStarted.compareAndSet(true, false)){
				consumer.closed();
				if(this.serverSocketChannel.isOpen()){
					this.serverSocketChannel.close();
				}
				if(this.socketChannel.isOpen()){
					this.socketChannel.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isClosed() {
		return !serverSocketChannel.isOpen()||!socketChannel.isOpen();
	}

}
