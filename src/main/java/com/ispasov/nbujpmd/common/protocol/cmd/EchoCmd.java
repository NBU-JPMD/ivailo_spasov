package com.ispasov.nbujpmd.common.protocol.cmd;

import com.ispasov.nbujpmd.common.protocol.ISMsg;
import com.ispasov.nbujpmd.common.protocol.IProtocolCmd;
import com.ispasov.nbujpmd.common.channel.ChannelHelper;

import java.io.IOException;

public class EchoCmd implements IProtocolCmd {
	private static final String[] FILTER = {"echo", "echoRsp"};

	@Override
	public boolean onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException {
		if (cmd.equals("echo")) {
			msg.addKey("type", "echoRsp");
			helper.getWriter().write(msg);
		} else if (cmd.equals("echoRsp")){
			System.out.println(msg);
		}
		return false;
	}

	@Override
	public String[] getFilters() {
		return FILTER;
	}
}