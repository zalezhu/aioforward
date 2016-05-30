package cn.com.cardinfo.forward.session;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.WritePendingException;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static Logger logger = LoggerFactory.getLogger(AioTcpClientSession.class);
	private AsynchronousSocketChannel socketChannel;
	private String id;
	// private AioWriteHandler writeHandler;
	private AtomicBoolean isStarted;
	private DateTime sendHeartBeatDate;
	private DateTime successSendDate;
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
		if (sendHeartBeatDate == null) {
			sendHeartBeatDate = DateTime.now();
		}
		if (successSendDate == null) {
			successSendDate =  DateTime.now();
		}
	}

	@Override
	public void process() {
		ExecutorsPool.FIXED_EXECUTORS.execute(this);

	}

	private void resetSendInterval(){
		sendHeartBeatDate =  DateTime.now();
	}
	public void resetSuccessInterval(){
		successSendDate =  DateTime.now();
	}
	private boolean needHeartbeat() {
		return sendHeartBeatDate.plusSeconds(HEART_BEAT_TIME).isBeforeNow();
	}
	private boolean needClose() {
		return successSendDate.plusSeconds(SUCCESS_TIME).isBeforeNow();
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
				logger.info("long time no data send close {}",id);
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
