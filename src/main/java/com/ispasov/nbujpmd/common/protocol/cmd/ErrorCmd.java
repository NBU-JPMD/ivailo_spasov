package com.ispasov.nbujpmd.common.protocol.cmd;

import com.ispasov.nbujpmd.common.protocol.ISMsg;
import com.ispasov.nbujpmd.common.protocol.IProtocolCmd;
import com.ispasov.nbujpmd.common.channel.ChannelHelper;

public class ErrorCmd implements IProtocolCmd {
	private static final String[] FILTER = {"error"};

	@Override
	public void onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) {
		System.out.println(msg);
	}

	@Override
	public String[] getFilters() {
		return FILTER;
	}
}