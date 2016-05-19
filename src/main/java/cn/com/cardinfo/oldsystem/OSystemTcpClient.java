package cn.com.cardinfo.oldsystem;

import java.nio.channels.AsynchronousChannelGroup;

import org.apache.log4j.Logger;

import cn.com.cardinfo.base.BaseTCP;
import cn.com.cardinfo.client.AioTcpClient;
import cn.com.cardinfo.util.Pools;
import cn.com.cardinfo.util.PropertiesUtil;

public class OSystemTcpClient extends BaseTCP{
	private static Logger logger = Logger.getLogger(OSystemTcpClient.class);
	private AioTcpClient oSystemClient;
	private AsynchronousChannelGroup asyncChannelGroup;
	public OSystemTcpClient(AsynchronousChannelGroup asyncChannelGroup){
		this.asyncChannelGroup = asyncChannelGroup;
		initDefault();
	}

	private void initDefault(){
		if(oSystemClient == null){
			oSystemClient = new AioTcpClient(Pools.OSYSTEM_CLIENT,asyncChannelGroup);
		}
	}

	public void start(){
		oSystemClient.startClient(PropertiesUtil.getOldsystemip(), PropertiesUtil.getOldsystemport());
	}
}
