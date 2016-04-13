package com.ispasov.nbujpmd.common.protocol.cmd;

import com.ispasov.nbujpmd.common.protocol.ISMsg;
import com.ispasov.nbujpmd.common.protocol.IProtocolCmd;
import com.ispasov.nbujpmd.common.channel.ChannelHelper;
import com.ispasov.nbujpmd.common.UserState;
import com.ispasov.nbujpmd.common.SendFile;

import java.io.IOException;

public class ReqPieceCmd implements IProtocolCmd {
	public boolean onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException {
		UserState userState  = (UserState)data;
		String user;

		synchronized (userState) {
			user = userState.getUser();
			SendFile sendFile = userState.getSendFile();
			if(user != null) {
				if (sendFile == null) {
					msg = new ISMsg();
					msg.addKey("type", "piece");
					msg.addKey("msg", "no file transfer is in progress");
					msg.setRespCode(601);
					helper.getWriter().write(msg);
					return false;
				}
				if(msg.getRespCode() == 0) {
					msg = sendFile.getNextMsg();
					if(sendFile.isFileSend()) {
						sendFile.close();
						userState.setSendFile(null);
					}
				} else {
					sendFile.close();
					userState.setSendFile(null);
					return false;
				}
			} else {
				msg = new ISMsg();
				msg.addKey("type", "piece");
				msg.addKey("msg", "user is not authenticated");
				msg.setRespCode(603);
				if(sendFile != null) {
					sendFile.close();
					userState.setSendFile(null);
				}
			}
		}
		helper.getWriter().write(msg);
		return false;
	}

	public String[] getFilters() {
		return new String[]{"reqPiece"};
	}
}
