import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

public class ClientAuthCommand implements ICommand {
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