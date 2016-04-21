package com.ispasov.nbujpmd.client;

import com.ispasov.nbujpmd.common.command.ICommand;
import com.ispasov.nbujpmd.common.command.ExitException;

import java.io.IOException;
import java.nio.channels.UnresolvedAddressException;

public class ClientCommand implements ICommand {
	private static final String[] FILTER = {"connect", "disconnect", "status"};

	private final Client client;

	public ClientCommand(Client client) {
		this.client = client;
	}

	@Override
	public boolean onCommand(String... args) throws ExitException {
		switch(args[0].toLowerCase()) {
			case "connect":
				if(client.isRunning()) {
					System.out.println("Client is already running.");
				} else {
					try {
						String host;
						int port;
						if(args.length > 1) {
							host = args[1];
						} else {
							host = Client.getDefaultHost();
						}
						if(args.length > 2) {
							try {
								port = Integer.parseInt(args[2]);
							} catch (NumberFormatException e) {
								port = 0;
							}
						} else {
							port = Client.getDefaultPort();
						}
						System.out.println("Connecting to " + host + ":" + port);
						client.startClient(host, port);
					} catch (IOException | UnresolvedAddressException ex){
						client.stopClient();
						System.out.println("Unable to connect: " + ex.getMessage());
					}
				}
				break;
			case "status":
				if(client.isRunning()) {
					System.out.println("Client is connected.");
				} else {
					System.out.println("Client is not connected.");
				}
				break;
			case "disconnect":
				if(client.isRunning()) {
					client.stopClient();
				} else {
					System.out.println("Client is not running.");
				}
				break;
		}
		return false;
	}

	@Override
	public String[] getFilters() {
		return FILTER;
	}

	@Override
	public String getCommandDescription(String cmd) {
		switch(cmd) {
			case "connect":
				return "Connect to server.";
			case "disconnect":
				return "Disconnect from server.";
			case "status":
				return "Show connection status";
		}
		return null;
	}
}