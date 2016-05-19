package cn.com.cardinfo.server;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import cn.com.cardinfo.event.Event;
import cn.com.cardinfo.event.EventPublish;
import cn.com.cardinfo.event.EventType;
import cn.com.cardinfo.util.HsmUtil;
import cn.com.cardinfo.util.Pools;

public class MsgConsumer implements Runnable {
	private static Logger logger = Logger.getLogger(MsgConsumer.class);
	private Queue<byte[]> msgQueue;
	private String fromQueueKey;
	private AtomicBoolean closed;

	public boolean isClosed() {
		return closed.get();
	}

	public void setClosed(boolean closed) {
		this.closed.set(closed);
	}

	public void closed() {
		this.setClosed(true);
	}

	public MsgConsumer(String fromQueueKey, Queue<byte[]> msgQueue) {
		this.fromQueueKey = fromQueueKey;
		this.msgQueue = msgQueue;
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
			if (Pools.UNION_SERVER.equals(fromQueueKey)) {
				if (dataBytes.length >= 44 && dataBytes[44] == 57) {
					if (Pools.SessionPool.get(Pools.NSYSTEM_CLIENT) != null
							&& !Pools.SessionPool.get(Pools.NSYSTEM_CLIENT).isClosed()) {
						push(Pools.NSYSTEM_CLIENT, dataBytes);
						logger.debug("send data to NEW_SYSTEM -->" + HsmUtil.bytesToHexString(dataBytes));
					} else {
						logger.debug("the client of the NEW_SYSTEM is not connected , So we do not send any data");
					}
				} else if (dataBytes.length >= 44) {
					if (Pools.SessionPool.get(Pools.OSYSTEM_CLIENT) != null
							&& !Pools.SessionPool.get(Pools.OSYSTEM_CLIENT).isClosed()) {
						push(Pools.OSYSTEM_CLIENT, dataBytes);
						logger.debug("send data to OLD_SYSTEM -->" + HsmUtil.bytesToHexString(dataBytes));
					} else {
						logger.debug("the client of the OLD_SYSTEM is not connected , So we do not send any data");
					}
				} else {
					// logger.info("send to both
					// "+HsmUtil.bytesToHexString(dataBytes));
					if (Pools.SessionPool.get(Pools.NSYSTEM_CLIENT) != null
							&& !Pools.SessionPool.get(Pools.NSYSTEM_CLIENT).isClosed()) {
						logger.info("send to NEW_SYSTEM " + HsmUtil.bytesToHexString(dataBytes));
						push(Pools.NSYSTEM_CLIENT, dataBytes);
					}
					if (Pools.SessionPool.get(Pools.OSYSTEM_CLIENT) != null
							&& !Pools.SessionPool.get(Pools.OSYSTEM_CLIENT).isClosed()) {
						logger.info("send to OLD_SYSTEM " + HsmUtil.bytesToHexString(dataBytes));
						push(Pools.OSYSTEM_CLIENT, dataBytes);
					}
				}
			} else {
				logger.info("send data to UNION_PAY--->" + HsmUtil.bytesToHexString(dataBytes));
				push(Pools.UNION_CLIENT, dataBytes);
			}
		}
		return remainBytes;
	}

	private void push(String to, byte[] bytes) {
		if (Pools.SessionPool.get(to) != null && !Pools.SessionPool.get(to).isClosed()) {
			Pools.SessionPool.get(to).getQueue().offer(bytes);
		} else {
			logger.info(to + " is closed,can not send data " + HsmUtil.bytesToHexString(bytes));
		}
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
						byte[] temp = new byte[qOutput.length + remainBytes.length];
						System.arraycopy(remainBytes, 0, temp, 0, remainBytes.length);
						System.arraycopy(qOutput, 0, temp, remainBytes.length, qOutput.length);
						qOutput = temp;
						remainBytes = null;
					}
				}else{
					qOutput = msgQueue.poll();
				}
				if (qOutput != null) {
					byte[] heads = Arrays.copyOfRange(qOutput, 0, 4);
					// logger.debug(fromQueueKey+"
					// heads--->"+HsmUtil.bytesToHexString(heads));
					int bodyLength = Integer.parseInt(new String(heads).trim());
					remainBytes = processMsg(qOutput, bodyLength + 4);
					if (remainBytes != null && remainBytes.length > 0) {
						logger.debug(fromQueueKey + " remainBytes--->" + HsmUtil.bytesToHexString(remainBytes));
					}
				}
				if (qOutput == null) {
					Thread.sleep(10);
				}
			} catch (Exception e) {
				logger.error("process message have exception for " + fromQueueKey, e);

				remainBytes = null;
				Pools.SessionPool.get(fromQueueKey).close();
				msgQueue.clear();
				EventPublish.publish(new Event(EventType.server_disconnect, fromQueueKey));
			}

		}
	}
}
