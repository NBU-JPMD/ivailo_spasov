package com.ispasov.nbujpmd.common.protocol.cmd;

import com.ispasov.nbujpmd.common.protocol.ISMsg;
import com.ispasov.nbujpmd.common.protocol.IProtocolCmd;
import com.ispasov.nbujpmd.common.channel.ChannelHelper;
import com.ispasov.nbujpmd.common.UserState;
import com.ispasov.nbujpmd.common.ReceiveFile;
import com.ispasov.nbujpmd.common.SearchManager;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PieceCmd implements IProtocolCmd {
	private static final Logger LOG = Logger.getLogger(PieceCmd.class.getName());
	private static final String[] FILTER = {"piece"};

	@Override
	public void onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException {
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
					return;
				}

				long piece = 0;
				byte[] byteData = null;
				try {
					piece = (long)msg.getData("piece");
					byteData = (byte[])msg.getData("data");
				} catch (Exception e) {
					LOG.log(Level.SEVERE, e.toString(), e);
				}

				try {
					receiveFile.write(byteData, piece);
					msg = new ISMsg();
					msg.addKey("type", "pieceRsp");

					if(receiveFile.isFileReceived()) {
						receiveFile.close();
						SearchManager seachManager = SearchManager.getInstance();
						seachManager.indexFile(receiveFile.getFileName());
						seachManager.save();
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
	}

	@Override
	public String[] getFilters() {
		return FILTER;
	}
}