package cn.com.cardinfo.event;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class AbstractEventSubscriber implements EventSubscriber{
	public void subscribe(Event event,EventNotifyType notifyType){

		if(event!=null){
			switch(notifyType){
			case once:{
				ConcurrentLinkedQueue<EventSubscriber> queue = EventPublish.getOnceQueue(event.getEventType().toString()+event.getTarget());
				if(queue==null){
					queue = new ConcurrentLinkedQueue<EventSubscriber>();
					EventPublish.putOnceQueue(event.getEventType().toString()+event.getTarget(),queue);
				}
				queue.offer(this);
				
				break;
			}
			case forever:
				ArrayList<EventSubscriber> list = EventPublish.getEveryTimeList(event.getEventType().toString());
				if(list==null){
					list = new ArrayList<EventSubscriber>();
					EventPublish.putEveryTimeList(event.getEventType().toString(), list);
				}
				list.add(this);
				break;
			}
		}
	
	}
}
