package cn.com.cardinfo.newsystem;

import java.nio.channels.AsynchronousChannelGroup;
import java.util.Arrays;

import cn.com.cardinfo.base.BaseTCP;
import cn.com.cardinfo.server.AioTcpServer;
import cn.com.cardinfo.util.Pools;
import cn.com.cardinfo.util.PropertiesUtil;

public class NSystemTcpServer extends BaseTCP{

	private AioTcpServer nsystemServer;
	private AsynchronousChannelGroup asyncChannelGroup;
	public NSystemTcpServer(AsynchronousChannelGroup asyncChannelGroup){
		this.asyncChannelGroup = asyncChannelGroup;
		initDefaults();
	}

	private void initDefaults() {
		
	}
	public void start(){
		nsystemServer = new AioTcpServer(PropertiesUtil.getNewsystembackport(),asyncChannelGroup,Pools.NSYSTEM_SERVER);
		nsystemServer.accept();	
	}

}
