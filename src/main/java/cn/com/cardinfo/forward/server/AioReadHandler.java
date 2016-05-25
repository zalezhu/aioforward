package cn.com.cardinfo.forward.server;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Arrays;

import org.apache.log4j.Logger;

import cn.com.cardinfo.forward.channel.Channel;
import cn.com.cardinfo.forward.event.Event;
import cn.com.cardinfo.forward.event.EventSource;
import cn.com.cardinfo.forward.event.EventSubscriber;
import cn.com.cardinfo.forward.event.EventType;
import cn.com.cardinfo.forward.session.AioTcpServerSession;
import cn.com.cardinfo.forward.util.HsmUtil; 
 /**
  * 
  * @author Zale
  *
  */
public class AioReadHandler extends EventSource implements CompletionHandler<Integer,AioTcpServerSession> { 
	private static Logger logger = Logger.getLogger(AioReadHandler.class);
    private String id;
    private String remoteAddress;
    private Channel channel;
    public AioReadHandler(AsynchronousSocketChannel socket,String id,Channel channel) { 
        try {
			remoteAddress = socket.getRemoteAddress().toString();
		} catch (IOException e) {
			remoteAddress = id;
		}
        this.id = id;
        this.channel = channel;
    } 
 
    public void cancelled(AioTcpServerSession session) { 
    	logger.info(id+" cancelled read msg"); 
    } 
 
	public void completed(Integer i, AioTcpServerSession session) {
		ByteBuffer buf = session.getReadBuffer();
		if (i > 0) {
			buf.flip();
			try {
				byte[] sendBytes = Arrays.copyOfRange(buf.array(), buf.position(), buf.limit());
				channel.getServerSession().getQueue().offer(sendBytes);
				logger.debug(id + " received " + remoteAddress + "'s msg:" + HsmUtil.bytesToHexString(sendBytes));
				buf.clear();
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(id + " received msg have exception", e);
			}
			session.read();
		} else if (i == -1) {
			publish(new Event(this,EventType.remote_client_disconnect));
			logger.debug(id + " client lost will restart:" + remoteAddress);
			buf = null;
		}
	} 
 
	public void subscribeClientDisconnect(EventSubscriber subscriber,NotifyType type){
		this.subscribe(subscriber, EventType.remote_client_disconnect, type);
	}
    public void failed(Throwable exc, AioTcpServerSession session) { 
    	logger.error("read msg failed "+id,exc); 
    	 publish(new Event(this,EventType.remote_client_disconnect));
    	 
    } 
}