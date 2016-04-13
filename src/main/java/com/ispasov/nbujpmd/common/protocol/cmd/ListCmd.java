package com.ispasov.nbujpmd.common.protocol.cmd;

import com.ispasov.nbujpmd.common.protocol.ISMsg;
import com.ispasov.nbujpmd.common.protocol.IProtocolCmd;
import com.ispasov.nbujpmd.common.channel.ChannelHelper;
import com.ispasov.nbujpmd.common.UserState;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;

public class ListCmd implements IProtocolCmd {
	public boolean onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException {
		msg = new ISMsg();
		UserState userState  = (UserState)data;
		String user;
		synchronized (userState) {
			user = userState.getUser();
		}

		if(user != null) {
			File folder = new File("upload/");
			ArrayList<String> filesArray = new ArrayList<String>();

			if(folder.exists() && folder.isDirectory()) {
				File[] listOfFiles = folder.listFiles();
				for (File file : listOfFiles) {
					if (file.isFile()) {
						filesArray.add(file.getName());
					}
				}
			} else {
				folder.mkdirs();
			}
			msg.addKey("type", "listRsp");
			msg.addKey("files", filesArray);
		} else {
			msg.addKey("type", "listRsp");
			msg.addKey("msg", "user is not authenticated");
			msg.setRespCode(101);
		}

		helper.getWriter().write(msg);
		return false;
	}

	public String[] getFilters() {
		return new String[]{"list"};
	}
}