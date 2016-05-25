package cn.com.cardinfo.forward.channel;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cardinfo.forward.event.Event;
import cn.com.cardinfo.forward.event.EventSource;
import cn.com.cardinfo.forward.event.EventSource.NotifyType;
import cn.com.cardinfo.forward.event.EventSubscriber;
import cn.com.cardinfo.forward.session.AioTcpClientSession;
import cn.com.cardinfo.forward.session.AioTcpServerSession;

/**
 * 
 * @author Zale
 *
 */
public class Channel implements EventSubscriber {
	private static Logger logger = LoggerFactory.getLogger(Channel.class);
	private String id;
	private TcpClientWrap client;
	private TcpServerWrap server;
	private AioTcpClientSession clientSession;
	private AioTcpServerSession serverSession;
	private ChannelType type;
	private AtomicBoolean isClientWork;
	private AtomicBoolean isServerWork;
	private Integer weigth;
	
	public Integer getWeigth() {
		return weigth;
	}

	public void setWeigth(Integer weigth) {
		this.weigth = weigth;
	}

	public boolean isWork() {
		return isClientWork.get()&&isServerWork.get();
	}

	public void setClientWork(boolean isClientWork) {
		this.isClientWork.set(isClientWork);
	}

	public void setServerWork(boolean isServerWork) {
		this.isServerWork.set(isServerWork);
	}

	public ChannelType getType() {
		return type;
	}

	public void setType(ChannelType type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public AioTcpClientSession getClientSession() {
		return clientSession;
	}

	public void setClientSession(AioTcpClientSession clientSession) {
		if (clientSession != null) {
			clientSession.subscribeRmtServerDisconnect(this, NotifyType.always);
		}
		this.clientSession = clientSession;
	}

	public void setServerSession(AioTcpServerSession serverSession) {
		if (serverSession != null) {
			serverSession.subscribeClientDisconnect(this, NotifyType.always);
		}
		this.serverSession = serverSession;
	}

	public AioTcpServerSession getServerSession() {
		return serverSession;
	}

	public TcpClientWrap getClient() {
		return client;
	}

	public void init(AsynchronousChannelGroup asyncChannelGroup, int clientPort, String serverIp, int serverPort) {
		if (isClientWork == null) {
			isClientWork = new AtomicBoolean(false);
		}
		if (isServerWork == null) {
			isServerWork = new AtomicBoolean(false);
		}
		createServer(asyncChannelGroup, clientPort);
		createClient(asyncChannelGroup, serverIp, serverPort);
		initEventSubscribe();
	}

	private void initEventSubscribe() {
		switch (type) {
		case union:
			server.getTcpServer().subscribeServerStarted(this, EventSource.NotifyType.always);
		case posp:
			server.getTcpServer().subscribeServerConnected(this, EventSource.NotifyType.always);
			break;
		}
		client.getTcpClient().subscribeClientConnected(this, NotifyType.always);
		client.getTcpClient().subscribeClientConnectedFailed(this, NotifyType.always);
	}

	private void createServer(AsynchronousChannelGroup asyncChannelGroup, int port) {
		server = new TcpServerWrap(asyncChannelGroup, port, id);
	}

	private void createClient(AsynchronousChannelGroup asyncChannelGroup, String ip, int port) {
		client = new TcpClientWrap(asyncChannelGroup, id, ip, port);
	}

	public TcpServerWrap getServer() {
		return server;
	}

	public void start() {
		server.start();

	}

	public static enum ChannelType {
		union, posp
	}

	@Override
	public void trigger(Event event) {
		switch (event.getEventType()) {
		case server_started:
			client.start();
			break;
		case remte_client_connected:
			if (type == ChannelType.posp) {
				client.start();
			}
			logger.info("{} Channel's Server is work,cause of remote client connect", id);
			setServerWork(true);
			break;
		case client_connected:
			logger.info("{} Channel's Client is work,cause of client connected", id);
			setClientWork(true);
			break;
		case remote_server_disconnect:
			setClientWork(false);
			logger.info("{} Channel'Client is not work,cause of remote server disconnect", id);
			if (type == ChannelType.posp) {
				serverSession.closeSocketClient();
			}
			if(type == ChannelType.union){
				client.start();
			}
			break;
		case remote_client_disconnect:
			setServerWork(false);
			logger.info("{} Channel'Server is not work,cause of remote client disconnect", id);
			if (type == ChannelType.posp) {
				// 当posp的接入的client断开，则断开接入posp的客户端，等待posp重连
				clientSession.close();
			} else {
				try {
					// 测试接出是否正常
					this.getClientSession().startHeartbeat();
				} catch (IOException e) {
					logger.error("ignore this exception ", e);
				}
			}
			// if(type == ChannelType.union){
			// client.start();
			// }
			break;
		case client_connected_failed:
			client.start();
			break;
		}

	}

	@Override
	public String getName() {
		return id;
	}
}
