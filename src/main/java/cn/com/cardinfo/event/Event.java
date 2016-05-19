package cn.com.cardinfo.event;

public class Event {
	private EventType eventType;
	
	private String target;

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public EventType getEventType() {
		return eventType;
	}

	public Event(EventType eventType, String target) {
		super();
		this.eventType = eventType;
		this.target = target;
	}

}
