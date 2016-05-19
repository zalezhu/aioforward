package cn.com.cardinfo.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import cn.com.cardinfo.event.Event;
import cn.com.cardinfo.event.EventPublish;
import cn.com.cardinfo.event.EventType;

public class AioTcpServer{
	private static  Logger logger = Logger.getLogger(AioAcceptHandler.class);
	private AsynchronousChannelGroup asyncChannelGroup;
	private AsynchronousServerSocketChannel serverSocketChannel;
	private String serverName;
	private AtomicBoolean isStarted;
	private int port;
	public boolean isAccept() {
		return serverSocketChannel.isOpen();
	}

	public AioTcpServer(int port, AsynchronousChannelGroup asyncChannelGroup, String serverName){
		this.serverName = serverName;
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
				serverSocketChannel.accept(serverSocketChannel, new AioAcceptHandler(serverName));
				EventPublish.publish(new Event(EventType.server_started,serverName));
			}else{
				logger.info(serverName+ " tcp server closed ");
			}
		}
	
}