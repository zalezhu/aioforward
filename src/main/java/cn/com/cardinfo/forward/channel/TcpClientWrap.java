package cn.com.cardinfo.forward.channel;

import java.nio.channels.AsynchronousChannelGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cardinfo.forward.base.BaseTCP;
import cn.com.cardinfo.forward.client.AioTcpClient;
/**
 * 
 * @author Zale
 *
 */
public class TcpClientWrap extends BaseTCP{
	private static Logger logger = LoggerFactory.getLogger(TcpServerWrap.class);
	private AioTcpClient tcpClient;
	private AsynchronousChannelGroup asyncChannelGroup;
	private String id;
	private String ip;
	private int port;
	public TcpClientWrap(AsynchronousChannelGroup asyncChannelGroup,String id,String ip,int port){
		this.asyncChannelGroup = asyncChannelGroup;
		this.id = id;
		this.port = port;
		this.ip = ip;
		initDefault();
	}

	private void initDefault(){
		if(tcpClient == null){
			tcpClient = new AioTcpClient(id,asyncChannelGroup);
		}
	}

	public void start(){
		tcpClient.startClient(ip, port);
		logger.info("Client of {} is started.IP is {},port is {}",id,ip,port);
	}

	public AioTcpClient getTcpClient() {
		return tcpClient;
	}
	


}
