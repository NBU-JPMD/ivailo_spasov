import java.io.IOException;
import java.nio.channels.UnresolvedAddressException;

public class ClientCommand implements ICommand {
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