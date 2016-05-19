package cn.com.cardinfo.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

public class AioTcpClient {
	private static Logger logger = Logger.getLogger(AioTcpClient.class);
	private AsynchronousChannelGroup asyncChannelGroup;
	private String name;
	private AsynchronousSocketChannel socketChannel;

	public AioTcpClient(String name,AsynchronousChannelGroup asyncChannelGroup){
		this.name = name;
		this.asyncChannelGroup = asyncChannelGroup;
		initDefaults();
	}
	private void initDefaults(){
		
	}
	
	public void startClient(final String ip, final int port) {
		try {
			if (socketChannel == null || !socketChannel.isOpen()) {
				socketChannel = AsynchronousSocketChannel.open(asyncChannelGroup);
				socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);
				socketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
//				connector.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
				socketChannel.connect(new InetSocketAddress(ip, port), socketChannel, new AioConnectHandler(name));
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("start client failed " + name, e);
		}
	}

	public void restart(final String ip, final int port) {
		
	}

}