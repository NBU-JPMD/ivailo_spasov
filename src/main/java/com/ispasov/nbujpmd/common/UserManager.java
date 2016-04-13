package com.ispasov.nbujpmd.common;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class UserManager implements Serializable {
	private static UserManager instance = null;
	private static String user_file = "users.dat";
	private HashMap<String, String> users = new HashMap<>();
	private transient HashSet<String> connected_users = new HashSet<>();
	static final long serialVersionUID = 69;

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
			try (FileInputStream fin = new FileInputStream(user_file);
				ObjectInputStream ois = new ObjectInputStream(fin)) {
				instance = (UserManager) ois.readObject();
				instance.connected_users = new HashSet<>();
			} catch (Exception e) {
				e.printStackTrace();
				instance = new UserManager();
				instance.save();
			}
		}
		return instance;
	}

	public synchronized void addUser(String user, String password) {
		users.put(user, getHashString(password));
	}

	public synchronized void delUser(String user) {
		users.remove(user);
	}

	public synchronized boolean connectUser(String user) {
		if(users.get(user) != null && !connected_users.contains(user)) {
			connected_users.add(user);
			return true;
		}
		return false;
	}

	public synchronized void disconnectUser(String user) {
		if(user != null) {
			connected_users.remove(user);
		}
	}

	public synchronized boolean isUserValid(String user, String password) {
		String ck_password = users.get(user);
		if(ck_password != null && ck_password.equals(getHashString(password))) {
			return true;
		}
		return false;
	}

	public synchronized String toString() {
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
		try (FileOutputStream fin = new FileOutputStream(user_file);
			ObjectOutputStream ois = new ObjectOutputStream(fin)) {
			ois.writeObject(this);
		} catch (Exception e) {
			e.printStackTrace();
			instance = new UserManager();
		}
	}
}
