package com.ispasov.nbujpmd.common.protocol;

import com.ispasov.nbujpmd.common.channel.ChannelHelper;
import com.ispasov.nbujpmd.common.protocol.cmd.ErrorCmd;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class SingleProtocolHandler implements IProtocolHandler {
	private static final Logger LOG = Logger.getLogger(SingleProtocolHandler.class.getName());
	private final List<IProtocolCmd> cmdList = new ArrayList<>();

	public SingleProtocolHandler() {
		registerCommand(new ErrorCmd());
	}

	@Override
	public void registerCommand(IProtocolCmd cmd) {
		cmdList.add(cmd);
	}

	@Override
	public void handleMsg(String type, ISMsg msg, ChannelHelper helper, Object data) {
		try {
			List<IProtocolCmd> runCmd = cmdList.stream()
				 .filter(f -> IProtocolCmd.matchCommand(type, f))
				 .collect(Collectors.toList());

			if(!runCmd.isEmpty()) {
				for(IProtocolCmd cmd : runCmd) {
					cmd.onCommand(type, msg, helper, data);
				}
			} else {
				msg = new ISMsg();
				msg.setRespCode(101);
				msg.addKey("type", "error");
				msg.addKey("msg", "Unsupported message type.");
				helper.getWriter().write(msg);
			}
		} catch (IOException ioe) {
			LOG.log(Level.SEVERE, ioe.toString(), ioe);
		}
	}

	@Override
	public void shutdown() {
	}
}