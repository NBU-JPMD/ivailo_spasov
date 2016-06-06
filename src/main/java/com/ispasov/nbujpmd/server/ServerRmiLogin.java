package com.ispasov.nbujpmd.server;

import com.ispasov.nbujpmd.common.IRmiLogin;
import com.ispasov.nbujpmd.common.IRmiAdmin;
import com.ispasov.nbujpmd.common.UserManager;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ServerRmiLogin extends UnicastRemoteObject implements IRmiLogin {
	private static final long serialVersionUID = 69;
	private final UserManager userManager = UserManager.getInstance();
	private Server server;

	public ServerRmiLogin(Server server) throws RemoteException {
		super();
		this.server = server;
	}

	public IRmiAdmin login(String username, String password) throws RemoteException {
		if(userManager.isUserValid(username, password) && userManager.isAdmin(username)) {
			return new ServerRmiAdmin(server);
		}
		return null;
	}
}