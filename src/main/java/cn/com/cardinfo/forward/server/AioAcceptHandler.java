package cn.com.cardinfo.forward.server;

import java.io.IOException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cardinfo.forward.base.Hub;
import cn.com.cardinfo.forward.channel.Channel;
import cn.com.cardinfo.forward.event.Event;
import cn.com.cardinfo.forward.event.EventSource;
import cn.com.cardinfo.forward.event.EventSubscriber;
import cn.com.cardinfo.forward.event.EventType;
import cn.com.cardinfo.forward.session.AioTcpServerSession;
import cn.com.cardinfo.forward.util.PropertiesUtil;
/**
 * 
 * @author Zale
 *
 */
public class AioAcceptHandler extends EventSource implements CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel> {
	private static Logger logger = LoggerFactory.getLogger(AioAcceptHandler.class);
	private String id;
	public AioAcceptHandler( String id) {
		super();
		this.id = id;
	}

	public void cancelled(AsynchronousServerSocketChannel attachment) {
		logger.info("accept cancelled");
	}

	public void completed(AsynchronousSocketChannel socket, AsynchronousServerSocketChannel attachment) {
		try {

			logger.debug(id+" have client linked :" + socket.getRemoteAddress().toString());
			publish(new Event(this,EventType.remte_client_connected));
			startRead(socket,attachment);
		} catch (IOException e) {
			logger.error("start read failed", e);
		} finally {
			accept(attachment);
		}
	}
	public void subscribeServerConnected(EventSubscriber subscriber,NotifyType type){
		subscribe(subscriber, EventType.remte_client_connected, type);
	}
	public void accept(AsynchronousServerSocketChannel serverSocketChannel) {
		if (serverSocketChannel.isOpen()) {
			serverSocketChannel.accept(serverSocketChannel, this);
		} else {
			logger.info("tcp server closed {}",id);
		}
	}

	public void failed(Throwable exc, AsynchronousServerSocketChannel attachment) {
		logger.error("accept failed", exc);
		accept(attachment);
	}

	public void startRead(AsynchronousSocketChannel socket, AsynchronousServerSocketChannel attachment) {
		if (socket.isOpen()) {
			Channel channel = Hub.getInstance().getChannel(id);
			AioTcpServerSession session = channel.getServerSession();
			if(session!=null){
				session.resetSocketChannel(socket);
			}else{
				session = new AioTcpServerSession(attachment,socket, PropertiesUtil.getReadBufferSize(),id);
				Hub.getInstance().getChannel(id).setServerSession(session);
			}
			session.process();
			
		}
	}
}