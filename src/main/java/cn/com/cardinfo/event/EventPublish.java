package cn.com.cardinfo.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import cn.com.cardinfo.server.AioAcceptHandler;

public class EventPublish {
	private static Map<String,ConcurrentLinkedQueue<EventSubscriber>> ONCE = new HashMap<String,ConcurrentLinkedQueue<EventSubscriber>>();
	private static Map<String,ArrayList<EventSubscriber>> EVERY_TIME = new HashMap<String,ArrayList<EventSubscriber>>();
	private static  Logger logger = Logger.getLogger(AioAcceptHandler.class);
	
	public static ConcurrentLinkedQueue<EventSubscriber> getOnceQueue(String key){
		return ONCE.get(key);
	}
	public static void putOnceQueue(String key,ConcurrentLinkedQueue<EventSubscriber> queue){
		ONCE.put(key, queue);
	}
	public static ArrayList<EventSubscriber> getEveryTimeList(String key){
		return EVERY_TIME.get(key);
	}
	public static void putEveryTimeList(String key,ArrayList<EventSubscriber> list){
		EVERY_TIME.put(key, list);
	}
	
	public static void publish(Event event){
		if (event != null) {
			Queue<EventSubscriber> queue = ONCE.get(event.getEventType().toString() + event.getTarget());
			while (queue != null && queue.peek() != null) {
				EventSubscriber subscriber = queue.poll();
				try{
					subscriber.trigger(event);
				}catch(Exception e){
					logger.error(subscriber.getAddr()+"-"+event.getEventType().toString()+"Exception: ",e);
				}
			}
			List<EventSubscriber> list = EVERY_TIME.get(event.getEventType().toString());
			if (list != null) {
				for (EventSubscriber subscriber : list) {
					if (subscriber.getAddr().equals(event.getTarget())) {
						try{
							subscriber.trigger(event);
						}catch(Exception e){
							logger.error(subscriber.getAddr()+"-"+event.getEventType().toString()+"Exception: ",e);
						}
					}
				}
			}
		}
	}
	
}
