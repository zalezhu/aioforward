package cn.com.cardinfo.server;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import cn.com.cardinfo.event.Event;
import cn.com.cardinfo.event.EventPublish;
import cn.com.cardinfo.event.EventType;
import cn.com.cardinfo.session.AioTcpServerSession;
import cn.com.cardinfo.util.HsmUtil;
import cn.com.cardinfo.util.Pools; 
 
public class AioReadHandler implements CompletionHandler<Integer,AioTcpServerSession> { 
	private static Logger logger = Logger.getLogger(AioReadHandler.class);
    private String serverName;
    private String remoteAddress;
    public AioReadHandler(AsynchronousSocketChannel socket,String serverName) { 
        try {
			remoteAddress = socket.getRemoteAddress().toString();
		} catch (IOException e) {
			remoteAddress = serverName;
		}
        this.serverName = serverName;
    } 
 
    public void cancelled(AioTcpServerSession session) { 
    	logger.info(serverName+" cancelled read msg"); 
    } 
 
	public void completed(Integer i, AioTcpServerSession session) {
		ByteBuffer buf = session.getReadBuffer();
		if (i > 0) {
			buf.flip();
			try {
				byte[] sendBytes = Arrays.copyOfRange(buf.array(), buf.position(), buf.limit());
				Pools.SessionPool.get(serverName).getQueue().offer(sendBytes);
				logger.debug(serverName + " received " + remoteAddress + "'s msg:" + HsmUtil.bytesToHexString(sendBytes));
				buf.clear();
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(serverName + " received msg have exception", e);
			}
			session.read();
		} else if (i == -1) {
			EventPublish.publish(new Event(EventType.server_disconnect, serverName));
			logger.debug(serverName + " client lost will restart:" + remoteAddress);
			buf = null;
		}
	} 
 
    public void failed(Throwable exc, AioTcpServerSession session) { 
    	logger.error(serverName+" read msg failed "+serverName,exc); 
    	 EventPublish.publish(new Event(EventType.server_disconnect,serverName));
    	 
    } 
}