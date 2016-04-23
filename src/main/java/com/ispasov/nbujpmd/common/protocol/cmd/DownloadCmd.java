package com.ispasov.nbujpmd.common.protocol.cmd;

import com.ispasov.nbujpmd.common.protocol.ISMsg;
import com.ispasov.nbujpmd.common.protocol.IProtocolCmd;
import com.ispasov.nbujpmd.common.channel.ChannelHelper;
import com.ispasov.nbujpmd.common.UserState;
import com.ispasov.nbujpmd.common.SendFile;

import java.io.IOException;

public class DownloadCmd implements IProtocolCmd {
	private static final String[] FILTER = {"download"};

	@Override
	public void onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException {
		UserState userState  = (UserState)data;
		String user;

		synchronized (userState) {
			user = userState.getUser();
			if(user != null) {
				SendFile sendFile = userState.getSendFile();
				if (sendFile != null) {
					msg = new ISMsg();
					msg.addKey("type", "downloadRsp");
					msg.addKey("msg", "file transfer is in progress");
					msg.setRespCode(501);
					helper.getWriter().write(msg);
					return;
				}
				String file = null;
				try {
					file = (String)msg.getData("file");
				} catch (Exception e) {
				}
				try {
					sendFile = new SendFile("upload/" + file, "downloadRsp");
					userState.setSendFile(sendFile);
					msg = sendFile.getUploadMsg();
				} catch (IOException ioe) {
					msg = new ISMsg();
					msg.addKey("type", "downloadRsp");
					msg.addKey("msg", "missing file");
					msg.setRespCode(502);
				}
			} else {
				msg = new ISMsg();
				msg.addKey("type", "downloadRsp");
				msg.addKey("msg", "user is not authenticated");
				msg.setRespCode(503);
			}
		}
		helper.getWriter().write(msg);
	}

	@Override
	public String[] getFilters() {
		return FILTER;
	}
}