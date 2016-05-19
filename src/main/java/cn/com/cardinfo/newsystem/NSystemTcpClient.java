package cn.com.cardinfo.newsystem;

import java.nio.channels.AsynchronousChannelGroup;

import org.apache.log4j.Logger;

import cn.com.cardinfo.base.BaseTCP;
import cn.com.cardinfo.client.AioTcpClient;
import cn.com.cardinfo.util.Pools;
import cn.com.cardinfo.util.PropertiesUtil;

public class NSystemTcpClient extends BaseTCP{
	private static Logger logger = Logger.getLogger(NSystemTcpClient.class);
	private AioTcpClient nSystemClient;
	private AsynchronousChannelGroup asyncChannelGroup;
	public NSystemTcpClient(AsynchronousChannelGroup asyncChannelGroup){
		this.asyncChannelGroup = asyncChannelGroup;
		initDefault();
	}

	private void initDefault(){
		if(nSystemClient == null){
			nSystemClient = new AioTcpClient(Pools.NSYSTEM_CLIENT,asyncChannelGroup);
		}
	}

	public void start(){
		nSystemClient.startClient(PropertiesUtil.getNewsystemip(), PropertiesUtil.getNewsystemport());
	}
}
