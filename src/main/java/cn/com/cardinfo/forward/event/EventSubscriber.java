package cn.com.cardinfo.forward.event;

import java.util.EventListener;
/**
 * 
 * @author Zale
 *
 */
public interface EventSubscriber extends EventListener{
	public void trigger(Event event);
	public String getName();
}
