package com.ispasov.nbujpmd.common.protocol.cmd;

import com.ispasov.nbujpmd.common.protocol.ISMsg;
import com.ispasov.nbujpmd.common.protocol.IProtocolCmd;
import com.ispasov.nbujpmd.common.channel.ChannelHelper;
import com.ispasov.nbujpmd.common.UserState;
import com.ispasov.nbujpmd.common.SendFile;

import java.io.IOException;

public class UploadRspCmd implements IProtocolCmd {
	public boolean onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException {
		UserState userState = (UserState)data;
		SendFile sendFIle = userState.getSendFile();
		if(msg.getRespCode() == 0) {
			if(sendFIle != null) {
				msg = sendFIle.getNextMsg();
				helper.getWriter().write(msg);
				if(sendFIle.isFileSend()) {
					System.out.println("sending file done");
					sendFIle.close();
					userState.setSendFile(null);
				}
			}
		} else {
			if(sendFIle != null) {
				sendFIle.close();
				userState.setSendFile(null);
			}
			System.out.println(msg);
		}
		return false;
	}

	public String[] getFilters() {
		return new String[]{"uploadRsp", "pieceRsp"};
	}
}