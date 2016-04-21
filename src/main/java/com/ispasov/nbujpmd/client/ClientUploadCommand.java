package com.ispasov.nbujpmd.client;

import com.ispasov.nbujpmd.common.command.ICommand;
import com.ispasov.nbujpmd.common.command.ExitException;
import com.ispasov.nbujpmd.common.protocol.ISMsg;
import com.ispasov.nbujpmd.common.SendFile;
import com.ispasov.nbujpmd.common.UserState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

class ClientUploadCommand implements ICommand {
	private static final Logger LOG = Logger.getLogger(ClientUploadCommand.class.getName());
	private static final String[] FILTER = {"upload"};

	private final Client client;

	public ClientUploadCommand(Client client) {
		this.client = client;
	}

	@Override
	public boolean onCommand(String... args) throws ExitException {
		if(client.isRunning()) {
			UserState userState = client.getUserState();
			SendFile sendFIle  = userState.getSendFile();
			if(sendFIle == null) {
				try {
					if (args.length > 1) {
						sendFIle = new SendFile(args[1]);
					} else {
						BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
						System.out.println("Enter file path:");
						String file = br.readLine();
						sendFIle = new SendFile(file);
					}
					ISMsg msg = sendFIle.getUploadMsg();
					client.getUserState().getChannelHelper().getWriter().write(msg);
					userState.setSendFile(sendFIle);
					System.out.println("sending file...");
				} catch (IOException ioe) {
					LOG.log(Level.SEVERE, ioe.toString(), ioe);
					client.stopClient();
				}
			} else {
				System.out.println("File transfer is already running.");
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
		return "Upload file to the server.";
	}
}