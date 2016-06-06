package com.ispasov.nbujpmd.server;

import com.ispasov.nbujpmd.common.IRmiAdmin;
import com.ispasov.nbujpmd.common.UserManager;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class ServerRmiAdmin extends UnicastRemoteObject implements IRmiAdmin {
	private static final long serialVersionUID = 69;
	private final UserManager userManager = UserManager.getInstance();
	private Server server;

	public ServerRmiAdmin(Server server) throws RemoteException {
		super();
		this.server = server;
	}

	public void deleteUser(String user) throws RemoteException {
		userManager.delUser(user);
		userManager.save();
	}

	public List<String> getUsers() throws RemoteException {
		return userManager.getAllUsers();
	}

	public void changePassword(String username, String password) throws RemoteException {
		if(userManager.isUserValid(username)) {
			userManager.addUser(username, password);
			userManager.save();
		}
	}

	public void disconnectUser(String user) throws RemoteException {
		server.disconnectUser(user);
	}

	public String getInfo(String user) throws RemoteException {
		return user + " | is valid: " + userManager.isUserValid(user) +
			" | is connected: " + userManager.isConnected(user) +
			" | is admin: " + userManager.isAdmin(user);
	}

	public void logout() throws RemoteException {
		unexportObject(this, true);
	}
}