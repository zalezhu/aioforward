package cn.com.cardinfo.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ExecutorsPool {
	public static ExecutorService FIXED_EXECUTORS = Executors.newFixedThreadPool(10);
	public static ExecutorService CACHED_EXECUTORS = Executors.newCachedThreadPool();
	public static ScheduledExecutorService SCHEDULE_EXECUTORS = Executors.newScheduledThreadPool(2);
}
