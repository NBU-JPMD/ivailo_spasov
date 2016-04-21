package com.ispasov.nbujpmd.common.protocol.cmd;

import com.ispasov.nbujpmd.common.protocol.ISMsg;
import com.ispasov.nbujpmd.common.protocol.IProtocolCmd;
import com.ispasov.nbujpmd.common.channel.ChannelHelper;
import com.ispasov.nbujpmd.common.UserState;
import com.ispasov.nbujpmd.common.ReceiveFile;

import java.io.IOException;

public class ClientPieceCmd implements IProtocolCmd {
	private static final String[] FILTER = {"piece"};

	@Override
	public boolean onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException {
		UserState userState = (UserState)data;
		ReceiveFile receiveFile = userState.getReceiveFile();
		if(msg.getRespCode() == 0) {
			if(receiveFile != null) {
				long piece = 0;
				byte[] byteData = null;
				try {
					piece = (long)msg.getData("piece");
					byteData = (byte[])msg.getData("data");
				} catch (Exception e) {
				}
				try {
					receiveFile.write(byteData, piece);

					if(receiveFile.isFileReceived()) {
						receiveFile.close();
						userState.setReceiveFile(null);
						System.out.println("Receive file done");
						return false;
					}
					msg = new ISMsg();
					msg.addKey("type", "reqPiece");
				} catch (IllegalArgumentException iae) {
					msg = new ISMsg();
					msg.addKey("type", "reqPiece");
					msg.addKey("msg", "wrong piece number");
					msg.setRespCode(701);
				} catch (IOException ioe) {
					msg = new ISMsg();
					msg.addKey("type", "reqPiece");
					msg.addKey("msg", "write file exception");
					msg.setRespCode(702);
				}
			} else {
				msg = new ISMsg();
				msg.addKey("type", "reqPiece");
				msg.addKey("msg", "file transfer is not in progress");
				msg.setRespCode(703);
			}
		} else {
			if(receiveFile != null) {
				receiveFile.close();
				receiveFile.deleteFile();
				userState.setReceiveFile(null);
			}
			System.out.println(msg);
		}
		helper.getWriter().write(msg);
		return false;
	}

	@Override
	public String[] getFilters() {
		return FILTER;
	}
}