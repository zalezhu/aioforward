package cn.com.cardinfo.forward.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * @author Zale
 *
 */
public abstract class EventSource {
	private static Logger logger = LoggerFactory.getLogger(EventSource.class);
	private Map<String,ConcurrentLinkedQueue<EventSubscriber>> onceSubscribers = new HashMap<String,ConcurrentLinkedQueue<EventSubscriber>>();
	private Map<String,ArrayList<EventSubscriber>> foreverSubscribers = new HashMap<String,ArrayList<EventSubscriber>>();
	private void subscribeOnce(EventSubscriber subscribe, EventType eventType) {
		ConcurrentLinkedQueue<EventSubscriber> queue = onceSubscribers.get(eventType.toString());
		if(queue==null){
			queue = new ConcurrentLinkedQueue<EventSubscriber>();
			onceSubscribers.put(eventType.toString(),queue);
		}
		queue.offer(subscribe);
	}

	private void subscribeAlways(EventSubscriber subscribe, EventType eventType) {
		ArrayList<EventSubscriber> list = foreverSubscribers.get(eventType.toString());
		if(list==null){
			list = new ArrayList<EventSubscriber>();
			foreverSubscribers.put(eventType.toString(), list);
		}
		list.add(subscribe);
	}
	
	protected void subscribe(EventSubscriber subscriber, EventType eventType,NotifyType notifyType) {
		switch(notifyType){
		case once:
			subscribeOnce(subscriber, eventType);
			break;
		case always:
			subscribeAlways(subscriber, eventType);
			break;
		}
	}
	public static enum NotifyType{
		once,
		always
	}
	protected void publish(Event event) {

		if (event != null) {
			Queue<EventSubscriber> queue = onceSubscribers.get(event.getEventType().toString());
			while (queue != null && queue.peek() != null) {
				EventSubscriber subscriber = queue.poll();
				try{
					subscriber.trigger(event);
				}catch(Exception e){
					logger.error(subscriber.getName()+"-"+event.getEventType().toString()+"Exception: ",e);
				}
			}
			List<EventSubscriber> list = foreverSubscribers.get(event.getEventType().toString());
			if (list != null) {
				for (EventSubscriber subscriber : list) {
						try{
							subscriber.trigger(event);
						}catch(Exception e){
							logger.error(subscriber.getName()+"-"+event.getEventType().toString()+"Exception: ",e);
						}
				}
			}
		}
	
	}
}
