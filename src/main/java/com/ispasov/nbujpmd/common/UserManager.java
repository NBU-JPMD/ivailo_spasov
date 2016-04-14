package com.ispasov.nbujpmd.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class UserManager implements Serializable {
	private static final String userFile = "users.dat";
	private static final long serialVersionUID = 70;

	private static UserManager instance = null;
	private Map<String, String> users = new ConcurrentHashMap<>();
	private transient ConcurrentHashMap<String, Boolean> connectedUsers = new ConcurrentHashMap<>();

	protected UserManager() {
	}

	private static String bytesToHex(byte[] in) {
		final StringBuilder builder = new StringBuilder();
		for(byte b : in) {
			builder.append(String.format("%02x", b));
		}
		return builder.toString();
	}

	private static String getHashString(String str) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return bytesToHex(digest.digest(str.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			return str;
		}
	}

	public synchronized static UserManager getInstance() {
		if(instance == null) {
			try (FileInputStream fin = new FileInputStream(userFile);
				 ObjectInputStream ois = new ObjectInputStream(fin)) {
				instance = (UserManager) ois.readObject();
				instance.connectedUsers = new ConcurrentHashMap<>();
			} catch (Exception e) {
				System.out.println(userFile + " not found. Creating new empty file.");
				instance = new UserManager();
				instance.save();
			}
		}
		return instance;
	}

	public void addUser(String user, String password) {
		users.put(user, getHashString(password));
	}

	public void delUser(String user) {
		users.remove(user);
	}

	public boolean connectUser(String user) {
		if(connectedUsers.putIfAbsent(user, true) == null) {
			return true;
		}
		return false;
	}

	public void disconnectUser(String user) {
		if(user != null) {
			connectedUsers.remove(user, true);
		}
	}

	public boolean isUserValid(String user, String password) {
		String ck_password = users.get(user);
		if(ck_password != null && ck_password.equals(getHashString(password))) {
			return true;
		}
		return false;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("users:");
		for (Map.Entry<String, String> entry : users.entrySet()) {
			sb.append("\n");
			sb.append(entry.getKey());
			sb.append(":");
			sb.append(entry.getValue());
		}
		return sb.toString();
	}

	public synchronized void save() {
		try (FileOutputStream fin = new FileOutputStream(userFile);
			 ObjectOutputStream ois = new ObjectOutputStream(fin)) {
			ois.writeObject(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
