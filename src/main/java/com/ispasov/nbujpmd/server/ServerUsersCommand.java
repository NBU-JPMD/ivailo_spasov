package com.ispasov.nbujpmd.server;

import com.ispasov.nbujpmd.common.command.ICommand;
import com.ispasov.nbujpmd.common.command.ExitException;
import com.ispasov.nbujpmd.common.UserManager;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerUsersCommand implements ICommand {
	private static final Logger LOG = Logger.getLogger(ServerUsersCommand.class.getName());
	private static final String[] FILTER = {"add", "del", "users"};
	private final UserManager userManager = UserManager.getInstance();

	private void addUser() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Enter new user:");
			String user = br.readLine();
			System.out.println("password:");
			String password = br.readLine();
			userManager.addUser(user, password);
			userManager.save();
		} catch (IOException ioe) {
			LOG.log(Level.SEVERE, ioe.toString(), ioe);
		}
	}

	private void delUser() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Enter user to delete:");
			String user = br.readLine();
			userManager.delUser(user);
			userManager.save();
		} catch (IOException ioe) {
			LOG.log(Level.SEVERE, ioe.toString(), ioe);
		}
	}

	@Override
	public void onCommand(String... args) throws ExitException {
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
	}

	@Override
	public String[] getFilters() {
		return FILTER;
	}

	@Override
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