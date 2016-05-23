package cn.com.cardinfo.forward.base;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import cn.com.cardinfo.forward.channel.Channel;
import cn.com.cardinfo.forward.channel.ChannelConfig;
import cn.com.cardinfo.forward.channel.Channel.ChannelType;
import cn.com.cardinfo.forward.util.ChannelsUtil;
import cn.com.cardinfo.forward.util.ExecutorsPool;
import cn.com.cardinfo.forward.util.PropertiesUtil;

/**
 * 
 * @author Zale
 *
 */
public class Hub {
	private Map<String, Channel> unionChannels;
	private Map<String, Channel> pospChannels;
	private static Hub hub;

	private Hub() {
	}

	public static synchronized Hub getInstance() {
		if (hub == null) {
			hub = new Hub();
		}
		return hub;
	}

	public void startHub() throws IOException {
		initExecutors();
		loadChannels();
		if (unionChannels == null || unionChannels.isEmpty() || pospChannels == null || pospChannels.isEmpty()) {
			throw new RuntimeException("There is no channel.start hub failed.The system will stop");
		}

		startChannels();
	}

	public Channel getChannel(String id) {
		if (unionChannels.containsKey(id)) {
			return unionChannels.get(id);
		}
		return pospChannels.get(id);
	}

	public Collection<Channel> getUnionChannels() {
		return unionChannels.values();
	}

	public Collection<Channel> getPospChannels() {
		return pospChannels.values();
	}

	private void initExecutors() {

	}
	
	public Channel balanceGetUnionChannel() {
		Channel less = null;
		for (Channel channel : unionChannels.values()) {
			if (channel.isWork()) {
				if (less == null) {
					less = channel;
				} else {
					int lessSize = less.getClientSession().getQueue().size();
					int channelSize = channel.getClientSession().getQueue().size();
					if (lessSize > channelSize) {
						less = channel;
					}else if(lessSize == channelSize){
						int rIndex = RandomUtils.nextInt(10)%2;
						if(rIndex==1){
							less = channel;
						}
						
					}
					
				}
			}
		}
		return less;
	}

	private void startChannels() {
		// start union channel
		for (Channel channel : unionChannels.values()) {
			channel.start();
		}
		// start posp channel
		for (Channel channel : pospChannels.values()) {
			channel.start();
		}
	}

	private void loadChannels() throws IOException {
		if (unionChannels == null) {
			unionChannels = new HashMap<String, Channel>();
		}
		if (pospChannels == null) {
			pospChannels = new HashMap<String, Channel>();
		}
		String channelStr = ChannelsUtil.getChannelJson();
		if (StringUtils.isNotBlank(channelStr)) {
			List<ChannelConfig> configList = JSON.parseObject(channelStr, new TypeReference<List<ChannelConfig>>() {
			});
			for (ChannelConfig config : configList) {
				Channel channel = new Channel();
				channel.setId(config.getId());
				channel.setType(config.getType());
				channel.init(AsynchronousChannelGroup.withThreadPool(ExecutorsPool.CHANNEL_EXECUTORS),
						config.getClientPort(), config.getServerIp(), config.getServerPort());
				if (channel.getType() == ChannelType.union) {
					unionChannels.put(config.getId(), channel);
				} else if (channel.getType() == ChannelType.posp) {
					pospChannels.put(config.getId(), channel);
				}
			}
		}
	}

}
