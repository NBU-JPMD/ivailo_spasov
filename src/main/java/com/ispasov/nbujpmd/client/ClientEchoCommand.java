package com.ispasov.nbujpmd.client;

import com.ispasov.nbujpmd.common.command.ICommand;
import com.ispasov.nbujpmd.common.command.ExitException;
import com.ispasov.nbujpmd.common.protocol.ISMsg;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

class ClientEchoCommand implements ICommand {
	private static final Logger LOG = Logger.getLogger(ClientEchoCommand.class.getName());
	private static final String[] FILTER = {"echo"};

	private final Client client;

	public ClientEchoCommand(Client client) {
		this.client = client;
	}

	@Override
	public boolean onCommand(String... args) throws ExitException {
		if(client.isRunning()) {
			ISMsg msg = new ISMsg();
			msg.addKey("type", "echo");
			if (args.length > 1) {
				msg.addKey("msg", args[1]);
			} else {
				msg.addKey("random", (int)(Math.random()*1000)%1000);
			}
			try {
				client.getUserState().getChannelHelper().getWriter().write(msg);
			} catch (IOException ioe) {
				LOG.log(Level.SEVERE, ioe.toString(), ioe);
				client.stopClient();
			}
		} else {
			System.out.println("Client is not running.");
		}
		return false;
	}

	@Override
	public String[] getFilters() {
		return FILTER;
	}

	@Override
	public String getCommandDescription(String cmd) {
		return "Send echo command";
	}
}