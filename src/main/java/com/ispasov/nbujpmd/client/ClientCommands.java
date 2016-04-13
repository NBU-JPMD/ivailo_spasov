package com.ispasov.nbujpmd.client;

import com.ispasov.nbujpmd.common.command.ICommand;
import com.ispasov.nbujpmd.common.command.ExitException;
import com.ispasov.nbujpmd.common.protocol.ISMsg;
import com.ispasov.nbujpmd.common.SendFile;
import com.ispasov.nbujpmd.common.ReceiveFile;
import com.ispasov.nbujpmd.common.UserState;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.channels.UnresolvedAddressException;
import java.io.File;

class ClientCommand implements ICommand {
	private Client client;

	public ClientCommand(Client client) {
		this.client = client;
	}

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
					} catch (IOException ioe){
						ioe.printStackTrace();
						client.stopClient();
					} catch (UnresolvedAddressException ue) {
						ue.printStackTrace();
						client.stopClient();
					}
				}
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

	public String[] getFilters() {
		return new String[]{"connect", "disconnect", "status"};
	}

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

class ClientAuthCommand implements ICommand {
	private Client client;

	public ClientAuthCommand(Client client) {
		this.client = client;
	}

	public boolean onCommand(String... args) throws ExitException {
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
		} catch (IOException io) {
			io.printStackTrace();
		}
		return false;
	}

	public String[] getFilters() {
		return new String[]{"authenticate"};
	}

	public String getCommandDescription(String cmd) {
		return "Send authenticate command with username and password.";
	}
}

class ClientEchoCommand implements ICommand {
	private Client client;

	public ClientEchoCommand(Client client) {
		this.client = client;
	}

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
				ioe.printStackTrace();
				client.stopClient();
			}
		} else {
			System.out.println("Client is not running.");
		}
		return false;
	}

	public String[] getFilters() {
		return new String[]{"echo"};
	}

	public String getCommandDescription(String cmd) {
		return "Send echo command";
	}
}

class ClientListCommand implements ICommand {
	private Client client;

	public ClientListCommand(Client client) {
		this.client = client;
	}

	public boolean onCommand(String... args) throws ExitException {
		if(client.isRunning()) {
			ISMsg msg = new ISMsg();
			msg.addKey("type", "list");
			try {
				client.getUserState().getChannelHelper().getWriter().write(msg);
			} catch (IOException ioe) {
				ioe.printStackTrace();
				client.stopClient();
			}
		} else {
			System.out.println("Client is not running.");
		}
		return false;
	}

	public String[] getFilters() {
		return new String[]{"list"};
	}

	public String getCommandDescription(String cmd) {
		return "List server files.";
	}
}

class ClientUploadCommand implements ICommand {
	private Client client;

	public ClientUploadCommand(Client client) {
		this.client = client;
	}

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
					ioe.printStackTrace();
				}
			} else {
				System.out.println("File transfer is already running.");
			}
		} else {
			System.out.println("Client is not running.");
		}
		return false;
	}

	public String[] getFilters() {
		return new String[]{"upload"};
	}

	public String getCommandDescription(String cmd) {
		return "Upload file to the server.";
	}
}

class ClientDownloadCommand implements ICommand {
	private Client client;

	public ClientDownloadCommand(Client client) {
		this.client = client;
	}

	public boolean onCommand(String... args) throws ExitException {
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
					receiveFile = new ReceiveFile(file, 0, "download/");
					ISMsg msg = new ISMsg();
					msg.addKey("type", "download");
					msg.addKey("file", file);

					userState.setReceiveFile(receiveFile);
					client.getUserState().getChannelHelper().getWriter().write(msg);
					System.out.println("Receive file...");
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			} else {
				System.out.println("File transfer is already running.");
			}
		} else {
			System.out.println("Client is not running.");
		}
		return false;
	}

	public String[] getFilters() {
		return new String[]{"download"};
	}

	public String getCommandDescription(String cmd) {
		return "Dowload file from the server.";
	}
}