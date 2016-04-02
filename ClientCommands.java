import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.channels.UnresolvedAddressException;

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
				client.write(msg);
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
				client.write(msg);
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
				client.write(msg);
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