package com.ispasov.nbujpmd.server;

import com.ispasov.nbujpmd.common.command.ICommand;
import com.ispasov.nbujpmd.common.command.ExitException;

import java.io.IOException;

public class ServerCommand implements ICommand {
	private static final String[] FILTER = {"start", "stop", "status"};
	private final Server server;

	public ServerCommand(Server server) {
		this.server = server;
	}

	@Override
	public void onCommand(String... args) throws ExitException {
		switch(args[0].toLowerCase()) {
			case "start":
				if(server.isRunning()) {
					System.out.println("The server is already running.");
				} else {
					try {
						int port;
						if(args.length > 1) {
							try {
								port = Integer.parseInt(args[1]);
							} catch (NumberFormatException e) {
								port = 0;
							}
						} else {
							port = Server.getDefaultPort();
						}
						server.startServer(port);
					} catch (IOException ioe){
						server.stopServer();
						System.out.println("Unable to start server: " + ioe.getMessage());
					}
				}
				break;
			case "status":
				if(server.isRunning()) {
					System.out.println("The server is running.");
				} else {
					System.out.println("The server is not running.");
				}
				break;
			case "stop":
				if(server.isRunning()) {
					server.stopServer();
				} else {
					System.out.println("The server is not running.");
				}
				break;
		}
	}

	@Override
	public String[] getFilters() {
		return FILTER;
	}

	@Override
	public String getCommandDescription(String cmd) {
		switch(cmd) {
			case "start":
				return "Start server.";
			case "stop":
				return "Stop server.";
			case "status":
				return "Show server status";
		}
		return null;
	}
}