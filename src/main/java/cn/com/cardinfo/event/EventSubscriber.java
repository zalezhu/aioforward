package cn.com.cardinfo.event;

public interface EventSubscriber {
	public void trigger(Event event);
	
	public void subscribe(Event event,EventNotifyType notifyType);
	
	public String getAddr();
}
