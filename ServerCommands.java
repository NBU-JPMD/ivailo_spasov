import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.channels.UnresolvedAddressException;

class ServerCommand implements ICommand {
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

class ServerUsersCommand implements ICommand {
	private UserManager userManager;

	public ServerUsersCommand(UserManager userManager) {
		this.userManager = userManager;
	}

	private void addUser() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Enter new user:");
			String user = br.readLine();
			System.out.println("password:");
			String password = br.readLine();
			userManager.addUser(user, password);
			userManager.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void delUser() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Enter user to delete:");
			String user = br.readLine();
			userManager.delUser(user);
			userManager.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean onCommand(String... args) throws ExitException {
		switch(args[0].toLowerCase()) {
			case "add":
				addUser();
				break;
			case "del":
				delUser();
				break;
			case "users":
				System.out.println(userManager.toString());
				break;
		}
		return false;
	}

	public String[] getFilters() {
		return new String[]{"add", "del", "users"};
	}

	public String getCommandDescription(String cmd) {
		switch(cmd) {
			case "add":
				return "Add new user.";
			case "del":
				return "Delete user.";
			case "users":
				return "List all users.";
		}
		return null;
	}
}