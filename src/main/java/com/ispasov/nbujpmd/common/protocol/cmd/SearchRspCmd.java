package com.ispasov.nbujpmd.common.protocol.cmd;

import com.ispasov.nbujpmd.common.protocol.ISMsg;
import com.ispasov.nbujpmd.common.protocol.IProtocolCmd;
import com.ispasov.nbujpmd.common.channel.ChannelHelper;

import java.io.IOException;
import java.util.List;

public class SearchRspCmd implements IProtocolCmd {
	private static final String[] FILTER = {"searchRsp"};

	@Override
	@SuppressWarnings("unchecked")
	public void onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException {
		if(msg.getRespCode() == 0) {
			List<String> fileList = null;
			try {
				fileList = (List<String>)msg.getData("files");
			} catch (Exception e) {
			}
			if(fileList != null) {
				System.out.println("Keyword found in:");
				fileList.forEach(System.out::println);
			} else {
				System.out.println("Files not found.");
			}
		} else {
			String strMsg = null;
			try {
				strMsg = (String)msg.getData("msg");
			} catch (Exception e) {
			}
			if(strMsg != null) {
				System.out.println(strMsg);
			} else {
				System.out.println(msg);
			}
		}
	}

	@Override
	public String[] getFilters() {
		return FILTER;
	}
}