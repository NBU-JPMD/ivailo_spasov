import java.io.IOException;

public class ClientEchoCommand implements ICommand {
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