package com.ispasov.nbujpmd.common.protocol.cmd;

import com.ispasov.nbujpmd.common.protocol.ISMsg;
import com.ispasov.nbujpmd.common.protocol.IProtocolCmd;
import com.ispasov.nbujpmd.common.channel.ChannelHelper;
import com.ispasov.nbujpmd.common.UserState;
import com.ispasov.nbujpmd.common.UserManager;

import java.io.IOException;

public class AuthCmd implements IProtocolCmd {
	private static final String[] FILTER = {"authenticate"};
	private final UserManager userManager = UserManager.getInstance();

	@Override
	public void onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException {
		String user = (String)msg.getData("user");
		String password = (String)msg.getData("password");
		msg = new ISMsg();
		UserState userState  = (UserState)data;
		synchronized (userState) {
			if(userState.getUser() != null) {
				msg.addKey("type", "authenticateRsp");
				msg.addKey("user", user);
				msg.addKey("msg", "user is already authenticated");
				msg.setRespCode(201);
				helper.getWriter().write(msg);
				return;
			}
			if(userManager.isUserValid(user, password)) {
				if(userManager.connectUser(user)) {
					msg.addKey("type", "authenticateRsp");
					msg.addKey("user", user);
					userState.setUser(user);
				} else {
					msg.addKey("type", "authenticateRsp");
					msg.addKey("user", user);
					msg.addKey("msg", "user already connected");
					msg.setRespCode(202);
				}
			} else {
				msg.addKey("type", "authenticateRsp");
				msg.addKey("user", user);
				msg.addKey("msg", "wrong username or password");
				msg.setRespCode(203);
			}
		}
		helper.getWriter().write(msg);
	}

	@Override
	public String[] getFilters() {
		return FILTER;
	}
}