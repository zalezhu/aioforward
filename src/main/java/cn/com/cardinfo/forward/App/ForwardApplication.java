package cn.com.cardinfo.forward.App;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cardinfo.forward.base.Hub;
/**
 * 
 * @author Zale
 *
 */
public class ForwardApplication {
	private static Logger logger = LoggerFactory.getLogger(ForwardApplication.class);
	public static void main(String[] args) {
//		try {
//			UnionTcpGroup union = new UnionTcpGroup();
//			union.start();
//			NSystemTcpGroup nSystem = new NSystemTcpGroup();
//			nSystem.start();
//			OSystemTcpGroup oSystem = new OSystemTcpGroup();
//			oSystem.start();
//			while(true){
//				Thread.sleep(2000);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			logger.error("start main application failed", e);
//		}
		
		
		try {
			Hub.getInstance().startHub();
			while(true){
				Thread.sleep(2000);
			}
		} catch (Exception e) {
			logger.error("Forward startup failed", e);
		}
	}
}
