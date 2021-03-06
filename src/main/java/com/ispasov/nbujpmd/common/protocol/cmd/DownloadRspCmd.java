package com.ispasov.nbujpmd.common.protocol.cmd;

import com.ispasov.nbujpmd.common.protocol.ISMsg;
import com.ispasov.nbujpmd.common.protocol.IProtocolCmd;
import com.ispasov.nbujpmd.common.channel.ChannelHelper;
import com.ispasov.nbujpmd.common.UserState;
import com.ispasov.nbujpmd.common.ReceiveFile;

import java.io.IOException;

public class DownloadRspCmd implements IProtocolCmd {
	private static final String[] FILTER = {"downloadRsp"};

	@Override
	public void onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException {
		UserState userState = (UserState)data;
		ReceiveFile receiveFile = userState.getReceiveFile();
		if(msg.getRespCode() == 0) {
			if(receiveFile != null) {
				long pieces = -1;
				long fileSize = -1;
				long piceSize = -1;
				try {
					pieces = (long)msg.getData("pieces");
					fileSize = (long)msg.getData("fileSize");
					piceSize = (long)msg.getData("piceSize");
				}catch (Exception e) {
				}
				msg = new ISMsg();
				msg.addKey("type", "reqPiece");
				try {
					receiveFile.setParams(pieces, fileSize, piceSize);
				} catch (IOException | IllegalArgumentException e) {
					receiveFile.close();
					receiveFile.deleteFile();
					userState.setReceiveFile(null);
					msg.setRespCode(600);
					System.out.println("Unable to receive file: " + e.getMessage());
				}

				helper.getWriter().write(msg);
			}
		} else {
			if(receiveFile != null) {
				receiveFile.close();
				receiveFile.deleteFile();
				userState.setReceiveFile(null);
			}
			System.out.println(msg);
		}
	}

	@Override
	public String[] getFilters() {
		return FILTER;
	}
}