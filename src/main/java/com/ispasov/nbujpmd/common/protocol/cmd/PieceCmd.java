package com.ispasov.nbujpmd.common.protocol.cmd;

import com.ispasov.nbujpmd.common.protocol.ISMsg;
import com.ispasov.nbujpmd.common.protocol.IProtocolCmd;
import com.ispasov.nbujpmd.common.channel.ChannelHelper;
import com.ispasov.nbujpmd.common.UserState;
import com.ispasov.nbujpmd.common.ReceiveFile;

import java.io.IOException;


public class PieceCmd implements IProtocolCmd {
	public boolean onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException {
		UserState userState  = (UserState)data;
		String user;

		synchronized (userState) {
			user = userState.getUser();
			if(user != null) {
				ReceiveFile receiveFile = userState.getReceiveFile();
				if (receiveFile == null) {
					msg = new ISMsg();
					msg.addKey("type", "pieceRsp");
					msg.addKey("msg", "file transfer is not in progress");
					msg.setRespCode(401);
					helper.getWriter().write(msg);
					return false;
				}

				long piece = 0;
				byte[] byteData = null;
				try {
					piece = (long)msg.getData("piece");
					byteData = (byte[])msg.getData("data");
				} catch (Exception e) {
				}

				try {
					receiveFile.write(byteData, piece);
					msg = new ISMsg();
					msg.addKey("type", "pieceRsp");

					if(receiveFile.isFileReceived()) {
						receiveFile.close();
						userState.setReceiveFile(null);
					}
				} catch (IllegalArgumentException iae) {
					msg = new ISMsg();
					msg.addKey("type", "pieceRsp");
					msg.addKey("msg", "wrong piece number");
					msg.setRespCode(402);
				} catch (IOException ioe) {
					msg = new ISMsg();
					msg.addKey("type", "pieceRsp");
					msg.addKey("msg", "write file exception");
					msg.setRespCode(403);
				}
			} else {
				msg = new ISMsg();
				msg.addKey("type", "pieceRsp");
				msg.addKey("msg", "user is not authenticated");
				msg.setRespCode(404);
			}
		}
		helper.getWriter().write(msg);
		return false;
	}
	public String[] getFilters() {
		return new String[]{"piece"};
	}
}