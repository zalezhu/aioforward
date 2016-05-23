package cn.com.cardinfo.forward.channel;

import cn.com.cardinfo.forward.channel.Channel.ChannelType;
/**
 * 
 * @author Zale
 *
 */
public class ChannelConfig {
	private String id;
	private String serverIp;
	private Integer serverPort;
	private Integer clientPort;
	private ChannelType type;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getServerIp() {
		return serverIp;
	}
	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}
	public Integer getServerPort() {
		return serverPort;
	}
	public void setServerPort(Integer serverPort) {
		this.serverPort = serverPort;
	}
	public Integer getClientPort() {
		return clientPort;
	}
	public void setClientPort(Integer clientPort) {
		this.clientPort = clientPort;
	}
	public ChannelType getType() {
		return type;
	}
	public void setType(ChannelType type) {
		this.type = type;
	}
}
