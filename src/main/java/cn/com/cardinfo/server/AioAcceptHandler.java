package cn.com.cardinfo.server;

import java.io.IOException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.List;

import org.apache.log4j.Logger;

import cn.com.cardinfo.event.Event;
import cn.com.cardinfo.event.EventPublish;
import cn.com.cardinfo.event.EventType;
import cn.com.cardinfo.session.AioTcpServerSession;
import cn.com.cardinfo.session.Session;
import cn.com.cardinfo.util.Pools;
import cn.com.cardinfo.util.PropertiesUtil;

public class AioAcceptHandler implements CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel> {
	private static Logger logger = Logger.getLogger(AioAcceptHandler.class);
	private String serverName;
	public AioAcceptHandler( String serverName) {
		super();
		this.serverName = serverName;
	}

	public void cancelled(AsynchronousServerSocketChannel attachment) {
		logger.info("accept cancelled");
	}

	public void completed(AsynchronousSocketChannel socket, AsynchronousServerSocketChannel attachment) {
		try {

			logger.debug(serverName+" have client linked :" + socket.getRemoteAddress().toString());
			EventPublish.publish(new Event(EventType.server_conneted,serverName));
			startRead(socket,attachment);
		} catch (IOException e) {
			logger.error("start read failed", e);
		} finally {
			accept(attachment);
		}
	}

	public void accept(AsynchronousServerSocketChannel serverSocketChannel) {
		if (serverSocketChannel.isOpen()) {
			serverSocketChannel.accept(serverSocketChannel, new AioAcceptHandler(serverName));
		} else {
			logger.info("tcp server closed " + serverName);
		}
	}

	public void failed(Throwable exc, AsynchronousServerSocketChannel attachment) {
		logger.error("accept failed", exc);
		accept(attachment);
	}

	public void startRead(AsynchronousSocketChannel socket, AsynchronousServerSocketChannel attachment) {
		if (socket.isOpen()) {
			Session session = new AioTcpServerSession(attachment,socket, PropertiesUtil.getReadBufferSize(),serverName);
			Pools.SessionPool.put(serverName, session);
			session.process();
			
		}
	}
}