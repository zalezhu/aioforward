package cn.com.cardinfo.forward.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
/**
 * 
 * @author Zale
 *
 */
public class ExecutorsPool {
	public static ExecutorService FIXED_EXECUTORS = Executors.newFixedThreadPool(10);
	public static ExecutorService CHANNEL_EXECUTORS = Executors.newCachedThreadPool();
	public static ExecutorService CACHED_EXECUTORS = Executors.newCachedThreadPool();
	public static ScheduledExecutorService SCHEDULE_EXECUTORS = Executors.newScheduledThreadPool(2);
	
}
