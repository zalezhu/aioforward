package cn.com.cardinfo.unionpay;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import cn.com.cardinfo.event.AbstractEventSubscriber;
import cn.com.cardinfo.event.Event;
import cn.com.cardinfo.event.EventNotifyType;
import cn.com.cardinfo.event.EventType;
import cn.com.cardinfo.session.Session;
import cn.com.cardinfo.util.Pools;

public class UnionTcpGroup extends AbstractEventSubscriber{
	private static Logger logger = Logger.getLogger(UnionTcpGroup.class);
	private UnionTcpServer tcpServer;
	private UnionTcpClient tcpClient;
	private AsynchronousChannelGroup asyncChannelGroup;
	private ExecutorService executor = Executors.newFixedThreadPool(20);
	private AtomicBoolean isStarted;
	
	public boolean isStarted() {
		return isStarted.get();
	}

	public void setStarted(boolean isStarted) {
		this.isStarted.set(isStarted);
	}

	public UnionTcpGroup() {
		initDefaults();
	}

	private void initDefaults(){
		if(isStarted == null){
			isStarted = new AtomicBoolean(false);
		}
		if(asyncChannelGroup == null){
			try {
				asyncChannelGroup = AsynchronousChannelGroup.withThreadPool(executor);
			} catch (IOException e) {
				logger.error("IOException",e);
			}
		}
		if(tcpServer == null){
			tcpServer = new UnionTcpServer(asyncChannelGroup);
		}
		if(tcpClient == null){
			tcpClient = new UnionTcpClient(asyncChannelGroup);
		}
		initEvent();
	}
	
	private void initEvent() {
		this.subscribe(new Event(EventType.server_disconnect,Pools.UNION_SERVER),EventNotifyType.forever);
		this.subscribe(new Event(EventType.client_disconnect,Pools.UNION_CLIENT),EventNotifyType.forever);
	}

	public void startServer(){
		tcpServer.start();
		
	}
	
	public void startClient(){
		tcpClient.start();
	}
	
	public void start(){
		this.subscribe(new Event(EventType.server_started,Pools.UNION_SERVER),EventNotifyType.once);
		startServer();
	}
	public void restart(){
		if(isStarted.compareAndSet(true, false)){
			Session serverSession = Pools.SessionPool.get(Pools.UNION_SERVER);
			if(serverSession != null){
				serverSession.close();
			}
			Session clientSession =Pools.SessionPool.get(Pools.UNION_CLIENT);
			if(clientSession!=null){
				clientSession.close();
			}
			start();
		}
	}

	@Override
	public void trigger(Event event) {
		switch(event.getEventType()){
			case server_started:
				logger.info(Pools.UNION_SERVER+" started then start client");
				startClient();
				setStarted(true);
			break;
			case server_disconnect:
				logger.info(Pools.UNION_SERVER+" will restart");
				restart();
			break;
		}
	}

	@Override
	public String getAddr() {
		return Pools.UNION_SERVER;
		
	}
}
