package com.ispasov.nbujpmd.common.protocol.cmd;

import com.ispasov.nbujpmd.common.protocol.ISMsg;
import com.ispasov.nbujpmd.common.protocol.IProtocolCmd;
import com.ispasov.nbujpmd.common.channel.ChannelHelper;
import com.ispasov.nbujpmd.common.UserState;
import com.ispasov.nbujpmd.common.ReceiveFile;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;

public class UploadCmd implements IProtocolCmd {
	private static final String[] FILTER = {"upload"};

	@Override
	public void onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException {
		UserState userState  = (UserState)data;
		String user;
		synchronized (userState) {
			user = userState.getUser();
			if(user != null) {
				ReceiveFile receiveFile = userState.getReceiveFile();
				if (receiveFile != null) {
					msg = new ISMsg();
					msg.addKey("type", "uploadRsp");
					msg.addKey("msg", "file transfer is already in progress");
					msg.setRespCode(303);
					helper.getWriter().write(msg);
					return;
				}

				String file = null;
				long pieces = -1;
				long fileSize = -1;
				long piceSize = -1;
				try {
					file = (String)msg.getData("file");
					pieces = (long)msg.getData("pieces");
					fileSize = (long)msg.getData("fileSize");
					piceSize = (long)msg.getData("piceSize");
				} catch (Exception e) {
				}
				if(file != null && file.endsWith(".txt")) {
					try {
						receiveFile = new ReceiveFile(file, "upload/", false);
						receiveFile.setParams(pieces, fileSize, piceSize);
						userState.setReceiveFile(receiveFile);
						msg = new ISMsg();
						msg.addKey("type", "uploadRsp");
						msg.addKey("file", file);
					} catch (IllegalArgumentException iae) {
						msg = new ISMsg();
						msg.addKey("type", "uploadRsp");
						msg.addKey("msg", iae.getMessage());
						msg.setRespCode(302);
					} catch (FileAlreadyExistsException fae) {
						msg = new ISMsg();
						msg.addKey("type", "uploadRsp");
						msg.addKey("msg", fae.getMessage());
						msg.setRespCode(301);
					}
				} else {
						msg = new ISMsg();
						msg.addKey("type", "uploadRsp");
						if(file != null) {
							msg.addKey("msg", "Unsupported file.");
						} else {
							msg.addKey("msg", "File name is missing.");
						}
						msg.setRespCode(305);
				}
			} else {
				msg = new ISMsg();
				msg.addKey("type", "uploadRsp");
				msg.addKey("msg", "user is not authenticated");
				msg.setRespCode(304);
			}
		}
		helper.getWriter().write(msg);
	}

	@Override
	public String[] getFilters() {
		return FILTER;
	}
}
