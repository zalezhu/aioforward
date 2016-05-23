package cn.com.cardinfo.forward.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cardinfo.forward.event.Event;
import cn.com.cardinfo.forward.event.EventSource;
import cn.com.cardinfo.forward.event.EventSubscriber;
import cn.com.cardinfo.forward.event.EventType;
/**
 * 
 * @author Zale
 *
 */
public class AioTcpServer extends EventSource implements EventSubscriber{
	private static  Logger logger = LoggerFactory.getLogger(AioAcceptHandler.class);
	private AsynchronousChannelGroup asyncChannelGroup;
	private AsynchronousServerSocketChannel serverSocketChannel;
	private String id;
	private AtomicBoolean isStarted;
	private int port;
	public boolean isAccept() {
		return serverSocketChannel.isOpen();
	}

	public AioTcpServer(int port, AsynchronousChannelGroup asyncChannelGroup, String id){
		this.id = id;
		this.asyncChannelGroup = asyncChannelGroup;
		this.port = port;
		initDefault();
		

	}
	private void initDefault(){
		if(isStarted == null){
			isStarted = new AtomicBoolean(false);
		}
		if(serverSocketChannel==null){
			try {
				serverSocketChannel = AsynchronousServerSocketChannel.open(asyncChannelGroup).bind(new InetSocketAddress(port),
						100);
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.setIsStarted(true);
		}
	}
	public boolean getIsStarted() {
		return isStarted.get();
	}

	public void setIsStarted(boolean isStarted) {
		this.isStarted.set(isStarted);
	}

	public void accept() {
			if (this.isStarted.get() && this.serverSocketChannel.isOpen()) {
				AioAcceptHandler handler =  new AioAcceptHandler(id);
				handler.subscribeServerConnected(this, NotifyType.always);
				serverSocketChannel.accept(serverSocketChannel,handler);
				publish(new Event(this,EventType.server_started));
			}else{
				logger.info(id+ " tcp server closed ");
			}
		}
	public void subscribeServerStarted(EventSubscriber subscriber,NotifyType notifyType){
		subscribe(subscriber, EventType.server_started, notifyType);
	}

	public void subscribeServerConnected(EventSubscriber subscriber,NotifyType notifyType){
		subscribe(subscriber, EventType.remte_client_connected, notifyType);
	}
	@Override
	public void trigger(Event event) {
			publish(event);
	}

	@Override
	public String getName() {
		return id+"-Server";
	}
}