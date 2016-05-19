package cn.com.cardinfo.util;

import java.util.HashMap;
import java.util.Map;

import cn.com.cardinfo.session.Session;

public class Pools {
	public static final String UNION_SERVER = "UNION_PAY_SERVER";
	public static final String UNION_CLIENT = "UNION_PAY_CLIENT";
	public static final String NSYSTEM_SERVER = "NEW_SYSTEM_SERVER";
	public static final String NSYSTEM_CLIENT = "NEW_SYSTEM_CLIENT";
	public static final String OSYSTEM_CLIENT = "OLD_SYSTEM_CLIENT";
	public static final String OSYSTEM_SERVER = "OLD_SYSTEM_SERVER";
	
	public static class SessionPool{
		public static final Map<String,Session> map = new HashMap<String,Session>();
		public static void put(String key,Session session){
			map.put(key,session);
		}
		public static Session get(String key){
			return map.get(key);
		}
	}
	
}
