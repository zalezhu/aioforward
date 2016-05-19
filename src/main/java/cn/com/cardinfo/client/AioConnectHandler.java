package cn.com.cardinfo.client;

import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.apache.log4j.Logger;

import cn.com.cardinfo.session.AioTcpClientSession;
import cn.com.cardinfo.session.Session;
import cn.com.cardinfo.util.Pools;

public class AioConnectHandler implements CompletionHandler<Void, AsynchronousSocketChannel> {
	private static Logger logger = Logger.getLogger(AioConnectHandler.class);
	private String name;

	public AioConnectHandler(String name) {
		this.name=name;
	}

	public void completed(Void attachment, AsynchronousSocketChannel connector) {
		logger.info(name +" connected  success!");
			startWrite(connector);
	}



	private void startWrite(AsynchronousSocketChannel socketChannel) {
		if (socketChannel.isOpen()) {
			Session session = new AioTcpClientSession(socketChannel, name);
			Pools.SessionPool.put(name, session);
			session.process();

		}
	}


	public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
		exc.printStackTrace();
		
	}




}