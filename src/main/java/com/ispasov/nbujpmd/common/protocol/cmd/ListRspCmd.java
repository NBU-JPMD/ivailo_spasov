package com.ispasov.nbujpmd.common.protocol.cmd;

import com.ispasov.nbujpmd.common.protocol.ISMsg;
import com.ispasov.nbujpmd.common.protocol.IProtocolCmd;
import com.ispasov.nbujpmd.common.channel.ChannelHelper;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class ListRspCmd implements IProtocolCmd {
	private static final String[] FILTER = {"listRsp"};

	@Override
	@SuppressWarnings("unchecked")
	public boolean onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException {
		if(msg.getRespCode() == 0) {
			System.out.println("Server files:");
			List<String> filesArray = (ArrayList<String>)msg.getData("files");
			filesArray.stream().forEach(file -> System.out.println(file));
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