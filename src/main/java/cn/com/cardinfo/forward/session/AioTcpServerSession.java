package cn.com.cardinfo.forward.session;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cardinfo.forward.event.Event;
import cn.com.cardinfo.forward.event.EventSubscriber;
import cn.com.cardinfo.forward.event.EventType;
import cn.com.cardinfo.forward.event.EventSource.NotifyType;
import cn.com.cardinfo.forward.server.AioReadHandler;
import cn.com.cardinfo.forward.server.MsgConsumer;
import cn.com.cardinfo.forward.util.ExecutorsPool;
/**
 * 
 * @author Zale
 *
 */
public class AioTcpServerSession extends AbstractTcpSession implements EventSubscriber{
	private static Logger logger = LoggerFactory.getLogger(AioTcpServerSession.class);
	private ByteBuffer readBuffer;
	private AtomicBoolean isStarted;
	private AsynchronousSocketChannel socketChannel;
	private AsynchronousServerSocketChannel serverSocketChannel;
	private AioReadHandler readHandler;
	private int bufferSize;
	private String id;
	private MsgConsumer consumer;
	private  Queue<byte[]> queue;
	public Queue<byte[]> getQueue() {
		return queue;
	}

	public void setQueue(Queue<byte[]> queue) {
		this.queue = queue;
	}

	
	public void resetSocketChannel(AsynchronousSocketChannel socketChannel) {
		this.socketChannel = socketChannel;
		setIsStarted(true);
	}

	public AioTcpServerSession(AsynchronousServerSocketChannel serverSocketChannel,AsynchronousSocketChannel socketChannel, int bufferSize,
			String id) {
		this.socketChannel = socketChannel;
		this.serverSocketChannel = serverSocketChannel;
		this.bufferSize = bufferSize;
		this.id = id;
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
			readHandler = new AioReadHandler(socketChannel, id);
			readHandler.subscribeClientDisconnect(this, NotifyType.always);
		}
		consumer = new MsgConsumer(id,getQueue());
		consumer.subscribeServerDisconnect(this, NotifyType.always);
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
	public void closeSocketClient(){

		try {
			if(isStarted.compareAndSet(true, false)){
				consumer.closed();
				if(this.socketChannel.isOpen()){
					this.socketChannel.close();
				}
			}
		} catch (IOException e) {
			//ignore this exception
//			e.printStackTrace();
		}
	
		
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
	public boolean isSocketServerClosed() {
		return !serverSocketChannel.isOpen();
	}
	public boolean isSocketClientClosed() {
		return !socketChannel.isOpen();
	}
	public boolean isClosed(){
		return isSocketServerClosed()&&isSocketClientClosed();
	}
	public void subscribeClientDisconnect(EventSubscriber subscriber,NotifyType type){
		this.subscribe(subscriber, EventType.remote_client_disconnect, type);
	}
	
	@Override
	public void trigger(Event event) {
		publish(event);
	}

	@Override
	public String getName() {
		return id+"-Server-Session";
	}

}
