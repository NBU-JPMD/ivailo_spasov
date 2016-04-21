package com.ispasov.nbujpmd.common.protocol.cmd;

import com.ispasov.nbujpmd.common.protocol.ISMsg;
import com.ispasov.nbujpmd.common.protocol.IProtocolCmd;
import com.ispasov.nbujpmd.common.channel.ChannelHelper;

public class AuthRspCmd implements IProtocolCmd {
	private static final String[] FILTER = {"authenticateRsp"};

	@Override
	public boolean onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) {
		if(msg.getRespCode() == 0) {
			System.out.println("user authenticated");
		} else {
			System.out.println(msg);
		}
		return false;
	}

	@Override
	public String[] getFilters() {
		return FILTER;
	}
}
