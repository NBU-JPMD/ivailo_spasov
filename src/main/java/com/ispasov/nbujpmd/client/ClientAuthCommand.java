package com.ispasov.nbujpmd.client;

import com.ispasov.nbujpmd.common.command.ICommand;
import com.ispasov.nbujpmd.common.command.ExitException;
import com.ispasov.nbujpmd.common.protocol.ISMsg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientAuthCommand implements ICommand {
	private static final Logger LOG = Logger.getLogger(ClientAuthCommand.class.getName());
	private static final String[] FILTER = {"authenticate"};

	private final Client client;

	public ClientAuthCommand(Client client) {
		this.client = client;
	}

	@Override
	public void onCommand(String... args) throws ExitException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			if(client.isRunning()) {
				String user;
				String password;
				if(args.length > 2) {
					user = args[1];
					password = args[2];
				} else {
					System.out.println("Enter user:");
					user = br.readLine();
					System.out.println("Enter password:");
					password = br.readLine();
				}
				ISMsg msg = new ISMsg();
				msg.addKey("type", "authenticate");
				msg.addKey("user", user);
				msg.addKey("password", password);
				client.getUserState().getChannelHelper().getWriter().write(msg);
			} else {
				System.out.println("Client is not running.");
			}
		} catch (IOException ioe) {
			LOG.log(Level.SEVERE, ioe.toString(), ioe);
			client.stopClient();
		}
	}

	@Override
	public String[] getFilters() {
		return FILTER;
	}

	@Override
	public String getCommandDescription(String cmd) {
		return "Send authenticate command with username and password.";
	}
}