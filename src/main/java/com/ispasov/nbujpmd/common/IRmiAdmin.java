package com.ispasov.nbujpmd.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IRmiAdmin extends Remote {
	public List<String> getUsers() throws RemoteException;
	public void deleteUser(String user) throws RemoteException;
	public void changePassword(String username, String password) throws RemoteException;
	public void disconnectUser(String user) throws RemoteException;
	public String getInfo(String user) throws RemoteException;
	public void logout() throws RemoteException;
}