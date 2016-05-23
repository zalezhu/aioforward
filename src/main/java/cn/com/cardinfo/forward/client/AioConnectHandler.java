package cn.com.cardinfo.forward.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cardinfo.forward.base.Hub;
import cn.com.cardinfo.forward.event.Event;
import cn.com.cardinfo.forward.event.EventSource;
import cn.com.cardinfo.forward.event.EventSubscriber;
import cn.com.cardinfo.forward.event.EventType;
import cn.com.cardinfo.forward.session.AioTcpClientSession;
/**
 * 
 * @author Zale
 *
 */
public class AioConnectHandler extends EventSource implements CompletionHandler<Void, AsynchronousSocketChannel> {
	private static Logger logger = LoggerFactory.getLogger(AioConnectHandler.class);
	private String id;
	private String ip;
	private int port;

	public AioConnectHandler(String id,String ip,int port) {
		this.id=id;
		this.ip=ip;
		this.port=port;
	}

	public void completed(Void attachment, AsynchronousSocketChannel connector) {
			logger.debug(id +" connected  success!");
			publish(new Event(this,EventType.client_connected));
			startWrite(connector);
	}

	public void subscribeClientConnected(EventSubscriber subscriber,NotifyType type){
		this.subscribe(subscriber, EventType.client_connected, type);
	}
	public void subscribeClientConnectedFailed(EventSubscriber subscriber,NotifyType type){
		this.subscribe(subscriber, EventType.client_connected_failed, type);
	}
	private void startWrite(AsynchronousSocketChannel socketChannel) {
		if (socketChannel.isOpen()) {
			AioTcpClientSession session = new AioTcpClientSession(socketChannel, id);
			Hub.getInstance().getChannel(id).setClientSession(session);
			session.process();
		}
	}


	public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
		logger.error("{} connect to server failed ",id,exc);
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		publish(new Event(this,EventType.client_connected_failed));
	}




}