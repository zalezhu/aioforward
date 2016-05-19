package cn.com.cardinfo.client;

import java.nio.channels.CompletionHandler;

import cn.com.cardinfo.event.Event;
import cn.com.cardinfo.event.EventPublish;
import cn.com.cardinfo.event.EventType;
import cn.com.cardinfo.session.AioTcpClientSession;

public class AioWriteHandler implements CompletionHandler<Integer,AioTcpClientSession>{
	private String name;
	
	public AioWriteHandler(String name) {
		super();
		this.name = name;
	}

	@Override
	public void completed(Integer result, AioTcpClientSession session) {
		session.resetSuccessInterval();
	}

	@Override
	public void failed(Throwable exc, AioTcpClientSession session) {
		session.close();
		EventPublish.publish(new Event(EventType.server_disconnect,name));
	}

}
