package com.ispasov.nbujpmd.common.protocol.cmd;

import com.ispasov.nbujpmd.common.protocol.ISMsg;
import com.ispasov.nbujpmd.common.protocol.IProtocolCmd;
import com.ispasov.nbujpmd.common.channel.ChannelHelper;
import com.ispasov.nbujpmd.common.UserState;
import com.ispasov.nbujpmd.common.SearchManager;

import java.io.IOException;
import java.util.List;

public class SearchCmd implements IProtocolCmd {
	private static final String[] FILTER = {"search"};

	@Override
	public void onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException {
		UserState userState  = (UserState)data;
		String user;
		synchronized (userState) {
			user = userState.getUser();
			if(user != null) {
				String keyword = null;
				try{
					keyword = (String)msg.getData("keyword");
				} catch(Exception e) {
				}
				if(keyword != null) {
					SearchManager seachManager = SearchManager.getInstance();
					msg = new ISMsg();
					msg.addKey("type", "searchRsp");
					List<String> fileList = seachManager.getFiles(keyword);
					if(fileList != null) {
						msg.addKey("files", fileList);
					} else {
						msg.setRespCode(903);
						msg.addKey("msg", "no files found");
					}
				} else {
					msg = new ISMsg();
					msg.addKey("type", "searchRsp");
					msg.addKey("msg", "missing keyword");
					msg.setRespCode(902);
				}

			} else {
				msg = new ISMsg();
				msg.addKey("type", "searchRsp");
				msg.addKey("msg", "user is not authenticated");
				msg.setRespCode(901);
			}
			helper.getWriter().write(msg);
		}
	}

	@Override
	public String[] getFilters() {
		return FILTER;
	}
}