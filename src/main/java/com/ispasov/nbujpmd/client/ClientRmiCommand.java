package com.ispasov.nbujpmd.client;

import com.ispasov.nbujpmd.common.command.ICommand;
import com.ispasov.nbujpmd.common.command.ExitException;
import com.ispasov.nbujpmd.common.IRmiAdmin;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.rmi.RemoteException;

class ClientRmiCommand implements ICommand {
	private static final Logger LOG = Logger.getLogger(ClientRmiCommand.class.getName());
	private static final String[] FILTER = {"rmiusers", "rmideluser", "rmipasswd", "rmidisconnect", "rmiinfo"};

	private final Client client;

	public ClientRmiCommand(Client client) {
		this.client = client;
	}

	private void listUsers(IRmiAdmin rmiAdmin) {
		try {
			System.out.println("User list:");
			rmiAdmin.getUsers().forEach(System.out::println);
		} catch (Exception e){
			System.out.println(e.getMessage());
		}
	}

	private void deleteUser(IRmiAdmin rmiAdmin) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Enter user to delete:");
			String user = br.readLine();
			rmiAdmin.deleteUser(user);
		} catch (RemoteException re){
			System.out.println(re.getMessage());
		} catch (IOException ioe) {
			LOG.log(Level.SEVERE, ioe.toString(), ioe);
		}
	}

	private void changePassword(IRmiAdmin rmiAdmin) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Enter new user:");
			String user = br.readLine();
			System.out.println("password:");
			String password = br.readLine();
			rmiAdmin.changePassword(user, password);
		} catch (RemoteException re){
			System.out.println(re.getMessage());
		} catch (IOException ioe) {
			LOG.log(Level.SEVERE, ioe.toString(), ioe);
		}
	}

	private void disconnectUser(IRmiAdmin rmiAdmin) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Enter user to disconnect:");
			String user = br.readLine();
			rmiAdmin.disconnectUser(user);
		} catch (RemoteException re){
			System.out.println(re.getMessage());
		} catch (IOException ioe) {
			LOG.log(Level.SEVERE, ioe.toString(), ioe);
		}
	}

	private void userInfo(IRmiAdmin rmiAdmin) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Enter user:");
			String user = br.readLine();
			System.out.println(rmiAdmin.getInfo(user));
		} catch (RemoteException re){
			System.out.println(re.getMessage());
		} catch (IOException ioe) {
			LOG.log(Level.SEVERE, ioe.toString(), ioe);
		}
	}

	@Override
	public void onCommand(String... args) throws ExitException {
		if(client.isRunning()) {
			IRmiAdmin rmiAdmin = client.getRmiAdmin();
			if(rmiAdmin == null) {
				System.out.println("Admin RMI is not connected.");
				return;
			}
			switch(args[0]) {
				case "rmiusers":
					listUsers(rmiAdmin);
					break;
				case "rmideluser":
					deleteUser(rmiAdmin);
					break;
				case "rmipasswd":
					changePassword(rmiAdmin);
					break;
				case "rmidisconnect":
					disconnectUser(rmiAdmin);
					break;
				case "rmiinfo":
					userInfo(rmiAdmin);
					break;
			}
		} else {
			System.out.println("Client is not running.");
		}
	}

	@Override
	public String[] getFilters() {
		return FILTER;
	}

	@Override
	public String getCommandDescription(String cmd) {
		switch(cmd) {
			case "rmiusers":
				return "Get user list";
			case "rmideluser":
				return "Delete user";
			case "rmipasswd":
				return "Change password";
			case "rmidisconnect":
				return "Disconnect user";
			case "rmiinfo":
				return "Get user info";
			default:
				return null;
		}
	}
}