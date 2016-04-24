package com.ispasov.nbujpmd.client;

import com.ispasov.nbujpmd.common.command.ICommand;
import com.ispasov.nbujpmd.common.command.ExitException;
import com.ispasov.nbujpmd.common.protocol.ISMsg;
import com.ispasov.nbujpmd.common.ReceiveFile;
import com.ispasov.nbujpmd.common.UserState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

class ClientDownloadCommand implements ICommand {
	private static final Logger LOG = Logger.getLogger(ClientDownloadCommand.class.getName());
	private static final String[] FILTER = {"download"};

	private final Client client;

	public ClientDownloadCommand(Client client) {
		this.client = client;
	}

	@Override
	public void onCommand(String... args) throws ExitException {
		if(client.isRunning()) {
			UserState userState = client.getUserState();
			ReceiveFile receiveFile  = userState.getReceiveFile();
			if(receiveFile == null) {
				try {
					String file;
					if (args.length > 1) {
						file = args[1];
					} else {
						BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
						System.out.println("Enter file name:");
						file = br.readLine();
					}
					receiveFile = new ReceiveFile(file, "download/", true);
					ISMsg msg = new ISMsg();
					msg.addKey("type", "download");
					msg.addKey("file", file);

					userState.setReceiveFile(receiveFile);
					client.getUserState().getChannelHelper().getWriter().write(msg);
					System.out.println("Receive file...");
				} catch (IOException ioe) {
					System.out.println("Unable to download file: " + ioe.getMessage());
				}
			} else {
				System.out.println("File transfer is already running.");
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
		return "Dowload file from the server.";
	}
}