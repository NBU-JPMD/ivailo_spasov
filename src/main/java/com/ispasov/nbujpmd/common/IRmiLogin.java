package com.ispasov.nbujpmd.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRmiLogin extends Remote {
	public IRmiAdmin login(String username, String password) throws RemoteException;
}