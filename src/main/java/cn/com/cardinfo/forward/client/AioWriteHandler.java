package cn.com.cardinfo.forward.client;

import java.nio.channels.CompletionHandler;

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
public class AioWriteHandler extends EventSource implements CompletionHandler<Integer,AioTcpClientSession>{
	
	public AioWriteHandler() {
		super();
	}

	@Override
	public void completed(Integer result, AioTcpClientSession session) {
		session.resetSuccessInterval();
	}

	@Override
	public void failed(Throwable exc, AioTcpClientSession session) {
		session.close();
		publish(new Event(this,EventType.remote_server_disconnect));
	}

	public void subscribeClientDisconnect(EventSubscriber subscriber,NotifyType type){
		this.subscribe(subscriber, EventType.remote_server_disconnect, type);
	}
}
