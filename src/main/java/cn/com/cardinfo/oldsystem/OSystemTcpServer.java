package cn.com.cardinfo.oldsystem;

import java.nio.channels.AsynchronousChannelGroup;
import java.util.Arrays;

import cn.com.cardinfo.base.BaseTCP;
import cn.com.cardinfo.server.AioTcpServer;
import cn.com.cardinfo.util.Pools;
import cn.com.cardinfo.util.PropertiesUtil;

public class OSystemTcpServer  extends BaseTCP{

	private AioTcpServer osystemServer;
	private AsynchronousChannelGroup asyncChannelGroup;
	public OSystemTcpServer(AsynchronousChannelGroup asyncChannelGroup){
		this.asyncChannelGroup = asyncChannelGroup;
		initDefaults();
	}

	private void initDefaults() {
		
	}
	public void start(){
		osystemServer = new AioTcpServer(PropertiesUtil.getOldsystembackport(),asyncChannelGroup,Pools.OSYSTEM_SERVER);
		osystemServer.accept();	
	}

}
