package cn.com.cardinfo.unionpay;

import java.nio.channels.AsynchronousChannelGroup;

import cn.com.cardinfo.base.BaseTCP;
import cn.com.cardinfo.server.AioTcpServer;
import cn.com.cardinfo.util.Pools;
import cn.com.cardinfo.util.PropertiesUtil;

public class UnionTcpServer extends BaseTCP{

	private AioTcpServer unionServer;
	private AsynchronousChannelGroup asyncChannelGroup;
	public UnionTcpServer(AsynchronousChannelGroup asyncChannelGroup){
		this.asyncChannelGroup = asyncChannelGroup;
		initDefaults();
	}

	private void initDefaults() {
		
	}
	@Override
	public void start() {
		unionServer = new AioTcpServer(PropertiesUtil.getUnionpaybackport(),asyncChannelGroup,Pools.UNION_SERVER);
		 unionServer.accept();			
	}

}
