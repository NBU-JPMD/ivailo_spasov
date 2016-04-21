package com.ispasov.nbujpmd.client;

import com.ispasov.nbujpmd.common.command.ICommand;
import com.ispasov.nbujpmd.common.command.ExitException;
import com.ispasov.nbujpmd.common.protocol.ISMsg;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

class ClientListCommand implements ICommand {
	private static final Logger LOG = Logger.getLogger(ClientListCommand.class.getName());
	private static final String[] FILTER = {"list"};

	private final Client client;

	public ClientListCommand(Client client) {
		this.client = client;
	}

	@Override
	public boolean onCommand(String... args) throws ExitException {
		if(client.isRunning()) {
			ISMsg msg = new ISMsg();
			msg.addKey("type", "list");
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
		return "List server files.";
	}
}