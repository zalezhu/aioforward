package cn.com.cardinfo.forward.server;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Arrays;

import org.apache.log4j.Logger;

import cn.com.cardinfo.forward.channel.Channel;
import cn.com.cardinfo.forward.event.Event;
import cn.com.cardinfo.forward.event.EventSource;
import cn.com.cardinfo.forward.event.EventSubscriber;
import cn.com.cardinfo.forward.event.EventType;
import cn.com.cardinfo.forward.session.AioTcpServerSession;
import cn.com.cardinfo.forward.util.HsmUtil; 
 /**
  * 
  * @author Zale
  *
  */
public class AioReadHandler extends EventSource implements CompletionHandler<Integer,AioTcpServerSession> { 
	private static Logger logger = Logger.getLogger(AioReadHandler.class);
    private String id;
    private String remoteAddress;
    private Channel channel;
    public AioReadHandler(AsynchronousSocketChannel socket,String id,Channel channel) { 
        try {
			remoteAddress = socket.getRemoteAddress().toString();
		} catch (IOException e) {
			remoteAddress = id;
		}
        this.id = id;
        this.channel = channel;
    } 
 
    public void cancelled(AioTcpServerSession session) { 
    	logger.info(id+" cancelled read msg"); 
    } 
    public static void main(String[] args) {
		byte[] bytes = new byte[]{1,2,3,4,5};
		System.out.println(Arrays.toString(HsmUtil.hexStringToByte("303433352E0234333520303030313030303020202034383431303030302020203030303030303030303030303B303030303030323030F23844C1B8E0981000000000000000413030303030303030303030303030303030323538303532353231323630373533393938363231323630373035323535373232303231303030363038343834313535313030383438343130303030333236353137363435424341333533424532333930423437334543303731413336453039363438434645314632433138373037424431353143374531443835423245393634323530303535394244364232373743304345313842453533333945353038433546353642434237314541463432374233393938373432463742433244373030353135303030303937373636353031303030313131383031353531303537323230303034CCC0ECBFB5C4D0A1B5EA202020202020202020202020202020202020202020202020202020202020313536C79DCCF22DE7E6A332363030303030303030303030303030303233303030303035303030333030303030303030303030313130323030303030303030363135303030303937373636354636353838373634")));
	}
	public void completed(Integer i, AioTcpServerSession session) {
		ByteBuffer buf = session.getReadBuffer();
		if (i > 0) {
			buf.flip();
			try {
				byte[] sendBytes = new byte[buf.limit()];
				buf.get(sendBytes, buf.position(), buf.limit());
				channel.getServerSession().getQueue().offer(sendBytes);
				logger.debug(id + " received " + remoteAddress + "'s msg:" + HsmUtil.bytesToHexString(sendBytes));
				buf.clear();
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(id + " received msg have exception", e);
			}
			session.read();
		} else if (i == -1) {
			publish(new Event(this,EventType.remote_client_disconnect));
			logger.debug(id + " client lost will restart:" + remoteAddress);
			buf = null;
		}
	} 
 
	public void subscribeClientDisconnect(EventSubscriber subscriber,NotifyType type){
		this.subscribe(subscriber, EventType.remote_client_disconnect, type);
	}
    public void failed(Throwable exc, AioTcpServerSession session) { 
    	logger.error("read msg failed "+id,exc); 
    	 publish(new Event(this,EventType.remote_client_disconnect));
    	 
    } 
}