package cn.com.cardinfo.unionpay;

import java.nio.channels.AsynchronousChannelGroup;

import org.apache.log4j.Logger;

import cn.com.cardinfo.base.BaseTCP;
import cn.com.cardinfo.client.AioTcpClient;
import cn.com.cardinfo.util.Pools;
import cn.com.cardinfo.util.PropertiesUtil;

public class UnionTcpClient extends BaseTCP{
	private static Logger logger = Logger.getLogger(UnionTcpClient.class);
	private AioTcpClient unionClient;
	private AsynchronousChannelGroup asyncChannelGroup;
	public UnionTcpClient(AsynchronousChannelGroup asyncChannelGroup){
		this.asyncChannelGroup = asyncChannelGroup;
		initDefault();
	}

	private void initDefault(){
		if(unionClient == null){
			unionClient = new AioTcpClient(Pools.UNION_CLIENT,asyncChannelGroup);
		}
	}

	public void start(){
		unionClient.startClient(PropertiesUtil.getUnionpayip(), PropertiesUtil.getUnionpayport());
	}
	
}
