package cn.com.cardinfo.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;

public class PropertiesUtil {
	private static Logger logger=Logger.getLogger(PropertiesUtil.class);
	private static PropertiesUtil configProperties;
	private static Properties properties;
	private static String propertiesPath = System.getProperty("user.home")+"/.cardinfo/gateway.properties";
	private static String backpropertiesPath = "./gateway.properties";
	private PropertiesUtil(){
	}
	
	public static PropertiesUtil getInstance(){
		synchronized (PropertiesUtil.class) {
			if(configProperties==null){
				configProperties=new PropertiesUtil();
				initProperties();
			}
		}
		return configProperties;
	}
	
	public String getConfig(String key){
		return properties.getProperty(key);
	}
	
	public int getConfigAsInteger(String key){
		return Integer.parseInt(properties.getProperty(key));
	}
	
	/**
	 * 得到以endWord结尾的属性集合
	 * @param endWord
	 * @return
	 */
	public HashMap<String, String> getConfigMatchEndWord(String endWord) {
		Enumeration<?> keys = properties.propertyNames();
		HashMap<String, String> resultConfigMap = new HashMap<String, String>();
		while(keys.hasMoreElements()){
			String key = keys.nextElement().toString();
			if(key.endsWith(endWord)) {
				resultConfigMap.put(key, getConfig(key));
			}
		}
		return resultConfigMap;
	}
	
	private static void initProperties(){
		properties=new Properties();

		InputStream inputStream=null;
		try{
			File file = new File(propertiesPath);
			if(!file.exists()){
				file = new File(backpropertiesPath);
			}
				inputStream= new FileInputStream(file);
			
			properties.load(inputStream);
		}catch (IOException e) {
			logger.error("",e);
		}finally{
			try {
				inputStream.close();
			} catch (Exception e) {
				logger.error("",e);
			}
		}
	}

	public static int getHeartbeatInterval() {
		return Integer.parseInt(PropertiesUtil.getInstance().getConfig("heart.beat.interval"));
	}
	public static String getUnionpayip() {
		return PropertiesUtil.getInstance().getConfig("unionpayip");
	}

	public static int getUnionpayport() {
		return Integer.parseInt(PropertiesUtil.getInstance().getConfig("unionpayport"));
	}


	public static int getUnionpaybackport() {
		return Integer.parseInt(PropertiesUtil.getInstance().getConfig("unionpaybackport"));
	}

	public static String getOldsystemip() {
		return PropertiesUtil.getInstance().getConfig("oldsystemip");
	}

	public static int getOldsystemport() {
		return Integer.parseInt(PropertiesUtil.getInstance().getConfig("oldsystemport"));
	}

	public static int getOldsystembackport() {
		return Integer.parseInt(PropertiesUtil.getInstance().getConfig("oldsystembackport"));
	}

	public static String getNewsystemip() {
		return PropertiesUtil.getInstance().getConfig("newsystemip");
	}

	public static int getNewsystemport() {
		return Integer.parseInt(PropertiesUtil.getInstance().getConfig("newsystemport"));
	}

	public static int getNewsystembackport() {
		return Integer.parseInt(PropertiesUtil.getInstance().getConfig("newsystembackport"));
	}
	
	public static int getReadBufferSize() {
		return Integer.parseInt(PropertiesUtil.getInstance().getConfig("read.buffer.size"));
	}

	
}
