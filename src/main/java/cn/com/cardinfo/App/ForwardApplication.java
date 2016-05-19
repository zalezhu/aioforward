package cn.com.cardinfo.App;


import org.apache.log4j.Logger;

import cn.com.cardinfo.newsystem.NSystemTcpGroup;
import cn.com.cardinfo.oldsystem.OSystemTcpGroup;
import cn.com.cardinfo.unionpay.UnionTcpGroup;

public class ForwardApplication {
	private static Logger logger = Logger.getLogger(ForwardApplication.class);
	public static void main(String[] args) {
		try {
			UnionTcpGroup union = new UnionTcpGroup();
			union.start();
			NSystemTcpGroup nSystem = new NSystemTcpGroup();
			nSystem.start();
			OSystemTcpGroup oSystem = new OSystemTcpGroup();
			oSystem.start();
			while(true){
				Thread.sleep(2000);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("start main application failed", e);
		}
	}
}
