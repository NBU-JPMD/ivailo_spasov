package com.ispasov.nbujpmd.common.protocol.cmd;

import com.ispasov.nbujpmd.common.protocol.ISMsg;
import com.ispasov.nbujpmd.common.protocol.IProtocolCmd;
import com.ispasov.nbujpmd.common.channel.ChannelHelper;

import java.io.IOException;
import java.util.ArrayList;

public class ListRspCmd implements IProtocolCmd {
	@SuppressWarnings("unchecked")
	public boolean onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException {
		if(msg.getRespCode() == 0) {
			System.out.println("Server files:");
			ArrayList<String> filesArray = (ArrayList<String>)msg.getData("files");
			for(String file : filesArray) {
				System.out.println(file);
			}
		} else {
			System.out.println(msg);
		}
		return false;
	}

	public String[] getFilters() {
		return new String[]{"listRsp"};
	}
}