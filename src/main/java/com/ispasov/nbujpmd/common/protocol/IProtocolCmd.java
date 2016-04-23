package com.ispasov.nbujpmd.common.protocol;

import com.ispasov.nbujpmd.common.channel.ChannelHelper;

import java.io.IOException;

public interface IProtocolCmd {
	public void onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException;
	public String[] getFilters();
}