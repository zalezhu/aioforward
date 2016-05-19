package cn.com.cardinfo.oldsystem;

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

public class OSystemTcpGroup extends AbstractEventSubscriber {
	private static Logger logger = Logger.getLogger(OSystemTcpGroup.class);
	private OSystemTcpServer tcpServer;
	private OSystemTcpClient tcpClient;
	private AsynchronousChannelGroup asyncChannelGroup;
	private ExecutorService executor = Executors.newFixedThreadPool(20);
	private AtomicBoolean isStarted;

	public boolean isStarted() {
		return isStarted.get();
	}

	public void setStarted(boolean isStarted) {
		this.isStarted.set(isStarted);
	}

	public boolean getIsStarted() {
		return isStarted.get();
	}

	public void setIsStarted(boolean isStarted) {
		this.isStarted.set(isStarted);
	}

	public OSystemTcpGroup() {
		initDefaults();
	}

	private void initDefaults() {
		if(isStarted == null){
			isStarted = new AtomicBoolean(false);
		}
		if (asyncChannelGroup == null) {
			try {
				asyncChannelGroup = AsynchronousChannelGroup.withThreadPool(executor);
			} catch (IOException e) {
				logger.error("IOException", e);
			}
		}
		if (tcpServer == null) {
			tcpServer = new OSystemTcpServer(asyncChannelGroup);
		}
		if (tcpClient == null) {
			tcpClient = new OSystemTcpClient(asyncChannelGroup);
		}
		initEvent();
	}

	private void initEvent() {
		this.subscribe(new Event(EventType.server_disconnect, Pools.OSYSTEM_SERVER), EventNotifyType.forever);
		this.subscribe(new Event(EventType.client_disconnect,Pools.UNION_CLIENT),EventNotifyType.forever);
	}

	public void startServer() {
		tcpServer.start();

	}

	public void startClient() {
		tcpClient.start();
	}

	public void start() {
		startServer();
		this.subscribe(new Event(EventType.server_conneted, Pools.OSYSTEM_SERVER), EventNotifyType.once);
		
	}

	public void restart() {
		if(isStarted.compareAndSet(true, false)){
			Session serverSession = Pools.SessionPool.get(Pools.OSYSTEM_SERVER);
			if (serverSession != null) {
				serverSession.close();
			}
	
			Session clientSession = Pools.SessionPool.get(Pools.OSYSTEM_CLIENT);
			if (serverSession != null) {
				clientSession.close();
			}
			start();
		}
	}

	@Override
	public void trigger(Event event) {
		switch (event.getEventType()) {
		case server_conneted:
			logger.info(Pools.OSYSTEM_SERVER +" connected then start client");
			startClient();
			setIsStarted(true);
			break;
		case client_disconnect:
		case server_disconnect:
			logger.info(Pools.OSYSTEM_SERVER +" will restart!");
			restart();
			break;
		}
	}

	@Override
	public String getAddr() {
		return Pools.OSYSTEM_SERVER;

	}
}
