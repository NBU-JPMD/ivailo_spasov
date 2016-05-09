package com.ispasov.nbujpmd.client;

import com.ispasov.nbujpmd.common.command.ICommand;
import com.ispasov.nbujpmd.common.command.ExitException;
import com.ispasov.nbujpmd.common.protocol.ISMsg;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

class ClientSearchCommand implements ICommand {
	private static final Logger LOG = Logger.getLogger(ClientSearchCommand.class.getName());
	private static final String[] FILTER = {"search"};

	private final Client client;

	public ClientSearchCommand(Client client) {
		this.client = client;
	}

	@Override
	public void onCommand(String... args) throws ExitException {
		if(client.isRunning()) {
			try {
				String keyword;
				if (args.length > 1) {
					keyword = args[1];
				} else {
					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
					System.out.println("Enter keyword:");
					keyword = br.readLine();
				}
				ISMsg msg = new ISMsg();
				msg.addKey("type", "search");
				msg.addKey("keyword", keyword);
				client.getUserState().getChannelHelper().getWriter().write(msg);
			} catch (IOException ioe) {
				System.out.println("Unable to search for file: " + ioe.getMessage());
			}
		} else {
			System.out.println("Client is not running.");
		}
	}

	@Override
	public String[] getFilters() {
		return FILTER;
	}

	@Override
	public String getCommandDescription(String cmd) {
		return "Search int the server for file by keyword";
	}
}