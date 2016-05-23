package cn.com.cardinfo.forward.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;

import org.apache.log4j.Logger;

import cn.com.cardinfo.forward.event.Event;
import cn.com.cardinfo.forward.event.EventSource;
import cn.com.cardinfo.forward.event.EventSubscriber;
import cn.com.cardinfo.forward.event.EventType;
/**
 * 
 * @author Zale
 *
 */
public class AioTcpClient extends EventSource implements EventSubscriber{
	private static Logger logger = Logger.getLogger(AioTcpClient.class);
	private AsynchronousChannelGroup asyncChannelGroup;
	private String id;
	private AsynchronousSocketChannel socketChannel;

	public AioTcpClient(String id,AsynchronousChannelGroup asyncChannelGroup){
		this.id = id;
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
				socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
				AioConnectHandler connectHandler = new AioConnectHandler(id,ip,port);
				connectHandler.subscribeClientConnected(this, NotifyType.always);
				connectHandler.subscribeClientConnectedFailed(this, NotifyType.always);
				socketChannel.connect(new InetSocketAddress(ip, port), socketChannel, connectHandler);
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("start client failed " + id, e);
		}
	}
	public void subscribeClientConnected(EventSubscriber subscriber,NotifyType type){
		this.subscribe(subscriber, EventType.client_connected, type);
	}
	public void subscribeClientConnectedFailed(EventSubscriber subscriber,NotifyType type){
		this.subscribe(subscriber, EventType.client_connected_failed, type);
	}
	public void restart(final String ip, final int port) {
		
	}
	@Override
	public void trigger(Event event) {
		publish(event);
	}
	@Override
	public String getName() {
		return id+"-Client";
	}

}