package cn.com.cardinfo.forward.channel;

import java.nio.channels.AsynchronousChannelGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cardinfo.forward.base.BaseTCP;
import cn.com.cardinfo.forward.server.AioTcpServer;
/**
 * 
 * @author Zale
 *
 */
public class TcpServerWrap extends BaseTCP{
	private static Logger logger = LoggerFactory.getLogger(TcpServerWrap.class);
	private AioTcpServer tcpServer;
	private AsynchronousChannelGroup asyncChannelGroup;
	private int port;
	private String id;
	protected TcpServerWrap(AsynchronousChannelGroup asyncChannelGroup,int port,String id){
		this.asyncChannelGroup = asyncChannelGroup;
		this.port = port;
		this.id = id;
		initDefaults();
	}

	private void initDefaults() {
		tcpServer = new AioTcpServer(port,asyncChannelGroup,id);
	}
	
	public AioTcpServer getTcpServer() {
		return tcpServer;
	}

	@Override
	public void start() {
		 tcpServer.accept();
		 logger.info("Server of {} is accpet.Port is {}",id,port);
	}

}
