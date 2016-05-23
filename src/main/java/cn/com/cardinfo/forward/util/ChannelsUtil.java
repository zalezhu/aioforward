package cn.com.cardinfo.forward.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * @author Zale
 *
 */
public class ChannelsUtil {
	private static Logger logger=LoggerFactory.getLogger(ChannelsUtil.class);
	private static ChannelsUtil channelUtil;
	private String channelJson;
	private String path = System.getProperty("user.home")+"/.cardinfo/channel.json";
	private String backupPath = "./channel.json";
	
	public ChannelsUtil() {
		init();
	}
	
	private static ChannelsUtil getInstance(){
		synchronized (ChannelsUtil.class) {
			if(channelUtil==null){
				channelUtil=new ChannelsUtil();
			}
		}
		return channelUtil;
	}
	
	private  void init() {

		BufferedReader input=null;
		try{
			File file = new File(path);
			if(!file.exists()){
				file = new File(backupPath);
			}
			input= new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line = null;
			
			while((line = input.readLine())!=null){
				if(channelJson == null){
					channelJson = "";
				}
				channelJson+=line;
			}
			logger.debug("union : {}",channelJson);
		}catch (IOException e) {
			logger.error("",e);
		}finally{
			try {
				input.close();
			} catch (Exception e) {
				logger.error("",e);
			}
		}
	
	}
	
	public static String getChannelJson(){
		return ChannelsUtil.getInstance().channelJson;
	}
	
}
