import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

public class ServerUsersCommand implements ICommand {
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