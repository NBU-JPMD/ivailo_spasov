package com.ispasov.nbujpmd.common.protocol;

import com.ispasov.nbujpmd.common.channel.ChannelHelper;

public interface IProtocolHandler {
	public void registerCommand(IProtocolCmd cmd);
	public void handleMsg(String type, ISMsg msg, ChannelHelper helper, Object data);
	public void shutdown();
}