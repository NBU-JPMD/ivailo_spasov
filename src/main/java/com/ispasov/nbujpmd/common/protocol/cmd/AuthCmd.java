package com.ispasov.nbujpmd.common.protocol.cmd;

import com.ispasov.nbujpmd.common.protocol.ISMsg;
import com.ispasov.nbujpmd.common.protocol.IProtocolCmd;
import com.ispasov.nbujpmd.common.channel.ChannelHelper;
import com.ispasov.nbujpmd.common.UserState;
import com.ispasov.nbujpmd.common.UserManager;

import java.io.IOException;

public class AuthCmd implements IProtocolCmd {
	private UserManager userManager = UserManager.getInstance();

	public boolean onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException {
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
				return false;
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
		return false;
	}

	public String[] getFilters() {
		return new String[]{"authenticate"};
	}
}