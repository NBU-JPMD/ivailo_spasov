import java.io.IOException;
import java.nio.channels.UnresolvedAddressException;

public class ServerCommand implements ICommand {
	private Server server;

	public ServerCommand(Server server) {
		this.server = server;
	}

	public boolean onCommand(String... args) throws ExitException {
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
						ioe.printStackTrace();
						server.stopServer();
					}
				}
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
		return false;
	}

	public String[] getFilters() {
		return new String[]{"start", "stop", "status"};
	}

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