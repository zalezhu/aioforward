package cn.com.cardinfo.forward.event;

import java.util.EventObject;
/**
 * 
 * @author Zale
 *
 */
public class Event extends EventObject{
	private static final long serialVersionUID = -1377066249969214396L;
	private EventType eventType;
	public EventType getEventType() {
		return eventType;
	}

	public Event(Object source,EventType eventType) {
		super(source);
		this.eventType = eventType;
	}

}
