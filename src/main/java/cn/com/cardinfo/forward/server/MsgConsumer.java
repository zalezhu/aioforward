package cn.com.cardinfo.forward.server;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cardinfo.forward.base.Hub;
import cn.com.cardinfo.forward.channel.Channel;
import cn.com.cardinfo.forward.event.Event;
import cn.com.cardinfo.forward.event.EventSource;
import cn.com.cardinfo.forward.event.EventSubscriber;
import cn.com.cardinfo.forward.event.EventType;
import cn.com.cardinfo.forward.session.AioTcpClientSession;
import cn.com.cardinfo.forward.util.HsmUtil;
/**
 * 
 * @author Zale
 *
 */
public class MsgConsumer extends EventSource implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(MsgConsumer.class);
	private Queue<byte[]> msgQueue;
	private String id;
	private AtomicBoolean closed;
	private Channel channel;
	
	

	public boolean isClosed() {
		return closed.get();
	}

	public void setClosed(boolean closed) {
		this.closed.set(closed);
	}

	public void closed() {
		this.setClosed(true);
	}

	public MsgConsumer(String id, Queue<byte[]> msgQueue,Channel channel) {
		this.id = id;
		this.msgQueue = msgQueue;
		this.channel = channel;
		initDefaults();
	}

	private void initDefaults() {
		if (closed == null) {
			closed = new AtomicBoolean(false);
		}
	}

	private byte[] processMsg(byte[] bytes, int length) {
		byte[] dataBytes = new byte[length];
		byte[] remainBytes = null;
		boolean output = true;
		if (bytes.length == length) {
			System.arraycopy(bytes, 0, dataBytes, 0, bytes.length);
		} else if (bytes.length > length) {
			System.arraycopy(bytes, 0, dataBytes, 0, dataBytes.length);
			remainBytes = Arrays.copyOfRange(bytes, dataBytes.length, bytes.length);
		} else {
			dataBytes = new byte[bytes.length];
			System.arraycopy(bytes, 0, dataBytes, 0, bytes.length);
			while (true) {
				bytes = msgQueue.poll();
				if (bytes != null) {
					// logger.debug("consumer2--->"+Arrays.toString(bytes));
					break;
				}
				
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			byte[] temp = new byte[dataBytes.length + bytes.length];
			System.arraycopy(dataBytes, 0, temp, 0, dataBytes.length);
			System.arraycopy(bytes, 0, temp, dataBytes.length, bytes.length);
			remainBytes = processMsg(temp, length);
			output = false;
		}
		if (output) {
			if (channel.getType()==Channel.ChannelType.union) {
				if (dataBytes.length >= 44) {
					Channel toChannel =Hub.getInstance().getPospChannel(String.valueOf(dataBytes[44]));
					if (toChannel!= null) {
						push(toChannel.getClientSession(), dataBytes);
						logger.debug("signle send data from union-{} to posp-{} --> {}",id,toChannel.getId(),  HsmUtil.bytesToHexString(dataBytes));
					} else {
						logger.info("the  channel {} is null , So we do not send any data {}" ,String.valueOf(dataBytes[44]));
					}
				} else {
					for(Channel toChannel:Hub.getInstance().getPospChannels()){
						if (toChannel!= null) {
							push(toChannel.getClientSession(), dataBytes);
							logger.debug("send data from union-{} to posp-{} --> {}",id,toChannel.getId(),  HsmUtil.bytesToHexString(dataBytes));
						} else {
							logger.info("{} the  channel is null , So we do not send any data",id);
						}
					}
				}
			} else {
				Channel toChannel = Hub.getInstance().balanceGetUnionChannel();
				if(toChannel!=null){
					push(toChannel.getClientSession(), dataBytes);
					logger.debug("send data from posp-{} to union-{}---> {}",id,toChannel.getId(), HsmUtil.bytesToHexString(dataBytes));
				}else{
					logger.info("All of the  union channel is null , So we do not send any data to union"); 
				}
			}
		}
		return remainBytes;
	}

	private void push(AioTcpClientSession to, byte[] bytes) {
		long start = System.currentTimeMillis();
		if (to!= null && !to.isSocketClosed()) {
			to.getQueue().offer(bytes);
		} else {
			logger.info("{} is closed,can not send data {}",to, HsmUtil.bytesToHexString(bytes));
		}
		long end = System.currentTimeMillis();
		logger.info("Use Time (PUSH) {} ms msg queue size {} ---{}",(end-start),msgQueue.size(),id);
	}
	public void subscribeServerDisconnect(EventSubscriber subscriber,NotifyType type){
		this.subscribe(subscriber, EventType.server_disconnect, type);
	}
	@Override
	public void run() {
		byte[] remainBytes = null;
		while (!isClosed()) {
			try {
				byte[] qOutput = null;
				if(remainBytes!=null){
					if(remainBytes.length>= 4){
						qOutput = remainBytes;
						remainBytes = null;
					}else{
						qOutput = msgQueue.poll();
						if(qOutput!=null){
							byte[] temp = new byte[qOutput.length + remainBytes.length];
							System.arraycopy(remainBytes, 0, temp, 0, remainBytes.length);
							System.arraycopy(qOutput, 0, temp, remainBytes.length, qOutput.length);
							qOutput = temp;
							remainBytes = null;
						}
					}
				}else{
					qOutput = msgQueue.poll();
				}
				if (qOutput != null) {
					byte[] heads = Arrays.copyOfRange(qOutput, 0, 4);
					int bodyLength = Integer.parseInt(new String(heads).trim());
					remainBytes = processMsg(qOutput, bodyLength + 4);
					logger.debug("msg queue size {} ---{}",msgQueue.size(),id);
					if (remainBytes != null && remainBytes.length > 0) {
						logger.debug(id + " remainBytes--->" + HsmUtil.bytesToHexString(remainBytes));
					}
				}
				if (qOutput == null) {
					Thread.sleep(1);
				}
			} catch (Exception e) {
				logger.error("process message have exception for " + id, e);

				remainBytes = null;
				channel.getServerSession().closeSocketClient();
				msgQueue.clear();
				publish(new Event(this,EventType.remote_client_disconnect));
			}

		}
	}
}
