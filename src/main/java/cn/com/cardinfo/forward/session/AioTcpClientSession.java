package cn.com.cardinfo.forward.session;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.WritePendingException;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.joda.time.Interval;

import cn.com.cardinfo.forward.client.AioWriteHandler;
import cn.com.cardinfo.forward.event.Event;
import cn.com.cardinfo.forward.event.EventSubscriber;
import cn.com.cardinfo.forward.event.EventType;
import cn.com.cardinfo.forward.util.ExecutorsPool;
/**
 * 
 * @author Zale
 *
 */
public class AioTcpClientSession extends AbstractTcpSession implements Runnable,EventSubscriber {
	private static Logger logger = Logger.getLogger(AioTcpClientSession.class);
	private AsynchronousSocketChannel socketChannel;
	private String id;
	// private AioWriteHandler writeHandler;
	private AtomicBoolean isStarted;
	private Interval sendinterval;
	private Interval successInterval;
	private String remoteAddress;
	private static final Integer HEART_BEAT_TIME = 60000;
	private static final Integer SUCCESS_TIME = 90000;
	private Queue<byte[]> queue;
	public Queue<byte[]> getQueue() {
		return queue;
	}

	public void setQueue(Queue<byte[]> queue) {
		this.queue = queue;
	}

	public AioTcpClientSession(AsynchronousSocketChannel SocketChannel, String id) {
		this.socketChannel = SocketChannel;
		try {
			remoteAddress = socketChannel.getRemoteAddress().toString();
		} catch (IOException e) {
			remoteAddress = id;
		}
		this.id = id;
		initDefaults();

	}
	

	private void initDefaults() {
		if (isStarted == null) {
			isStarted = new AtomicBoolean(true);
		}
		if(queue==null){
			this.queue = new ConcurrentLinkedQueue<byte[]>();
		}
		if (sendinterval == null) {
			Date now = new Date();
			sendinterval = new Interval(now.getTime(), now.getTime() + HEART_BEAT_TIME);
		}
		if (successInterval == null) {
			Date now = new Date();
			successInterval = new Interval(now.getTime(), now.getTime() + SUCCESS_TIME);
		}
	}

	@Override
	public void process() {
		ExecutorsPool.FIXED_EXECUTORS.execute(this);

	}

	private void resetSendInterval(){
		Date now = new Date();
		sendinterval = new Interval(now.getTime(), now.getTime() + HEART_BEAT_TIME);
	}
	public void resetSuccessInterval(){
		Date now = new Date();
		successInterval = new Interval(now.getTime(), now.getTime() + SUCCESS_TIME);
	}
	private boolean needHeartbeat() {
		Date now = new Date();
		return !sendinterval.contains(now.getTime());
	}
	private boolean needClose() {
		Date now = new Date();
		return !successInterval.contains(now.getTime());
	}
	public void startHeartbeat() throws IOException {
		write(ByteBuffer.wrap(new byte[] { (byte) 48, (byte) 48, (byte) 48, (byte) 48 }));
		logger.debug(" send heart beats to "+id+" -->" + remoteAddress);
		resetSendInterval();
	}

	private void write(ByteBuffer buffer) {
		if (isStarted()) {
			if(!needClose()){
				try{
					AioWriteHandler writeHandler = new AioWriteHandler();
					writeHandler.subscribeClientDisconnect(this, NotifyType.always);
					this.socketChannel.write(buffer,this,writeHandler);
				}catch(WritePendingException e){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					AioWriteHandler writeHandler = new AioWriteHandler();
					writeHandler.subscribeClientDisconnect(this, NotifyType.always);
					this.socketChannel.write(buffer,this,writeHandler);
				}
			}else{
				this.close();
//				publish(new Event(this,EventType.remote_server_disconnect));
			}
		} else {
			logger.info(id + " remote server has been closed");
		}
	}
	public void subscribeRmtServerDisconnect(EventSubscriber subscriber,NotifyType type){
		this.subscribe(subscriber, EventType.remote_server_disconnect, type);
	}
	
	public boolean isStarted() {
		return isStarted.get()&&!isSocketClosed();
	}

	public void setIsStarted(boolean isClosed) {
		this.isStarted.set(isClosed);
	}

	@Override
	public void run() {

		while (isStarted()) {
			byte[] qOutput = getQueue().poll();
			if (qOutput != null) {
				ByteBuffer buffer = ByteBuffer.wrap(qOutput);
				write(buffer);
				resetSendInterval();
			}
			if (qOutput == null) {
				if (needHeartbeat()) {
					try {
						startHeartbeat();
					} catch (IOException e) {
						logger.error(id + " heart beat failed", e);
					}
				}

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}

	}

	@Override
	public void close() {
		try {
			if (isStarted.compareAndSet(true, false)) {
				if(!isSocketClosed()){
					this.socketChannel.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isSocketClosed() {
		return !this.socketChannel.isOpen();
	}
	public boolean isClosed() {
		return isSocketClosed();
	}
	@Override
	public void trigger(Event event) {
		publish(event);
	}

	@Override
	public String getName() {
		return id+"-Client-Session";
	}
}
