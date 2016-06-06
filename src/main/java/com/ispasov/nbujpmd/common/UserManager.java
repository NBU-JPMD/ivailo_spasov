package com.ispasov.nbujpmd.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

class UserData implements Serializable {
	private static final long serialVersionUID = 69;
	private String password;
	private boolean isAdmin;

	public UserData(String password, boolean isAdmin) {
		this.password = password;
		this.isAdmin = isAdmin;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean getIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	@Override
	public String toString() {
		return password + ":" + isAdmin;
	}
}

public class UserManager implements Serializable {
	private static final Logger LOG = Logger.getLogger(UserManager.class.getName());
	private static final String USERFILE = "users.dat";
	private static final long serialVersionUID = 71;

	private static UserManager instance = null;
	private final Map<String, UserData> users = new ConcurrentHashMap<>();
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
			try (FileInputStream fin = new FileInputStream(USERFILE);
				 ObjectInputStream ois = new ObjectInputStream(fin)) {
				instance = (UserManager) ois.readObject();
				instance.connectedUsers = new ConcurrentHashMap<>();
			} catch (Exception e) {
				System.out.println(USERFILE + " not found. Creating new empty file.");
				instance = new UserManager();
				instance.save();
			}
		}
		return instance;
	}

	public List<String> getAllUsers() {
		return new ArrayList<String>(users.keySet());
	}

	public boolean isUserValid(String user) {
		return users.containsKey(user);
	}

	public void addUser(String user, String password) {
		addUser(user, password, false);
	}

	public void addUser(String user, String password, boolean admin) {
		UserData userData = users.putIfAbsent(user, new UserData(getHashString(password), admin));
		if(userData != null) {
			userData.setPassword(getHashString(password));
		}
	}

	public boolean isAdmin(String user) {
		UserData userData = users.get(user);
		if(userData == null)
			return false;
		return userData.getIsAdmin();
	}

	public void delUser(String user) {
		users.remove(user);
	}

	public boolean isConnected(String user) {
		return connectedUsers.containsKey(user);
	}

	public boolean connectUser(String user) {
		return connectedUsers.putIfAbsent(user, true) == null;
	}

	public void disconnectUser(String user) {
		if(user != null) {
			connectedUsers.remove(user, true);
		}
	}

	public boolean isUserValid(String user, String password) {
		UserData userData = users.get(user);
		if(userData == null)
			return false;
		String ck_password = userData.getPassword();
		return ck_password != null && ck_password.equals(getHashString(password));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("users:");
		users.forEach((k, v) -> {
			sb.append("\n");
			sb.append(k);
			sb.append(":");
			sb.append(v);
		});
		return sb.toString();
	}

	public synchronized void save() {
		try (FileOutputStream fin = new FileOutputStream(USERFILE);
			 ObjectOutputStream ois = new ObjectOutputStream(fin)) {
			ois.writeObject(this);
		} catch (Exception e) {
			LOG.log(Level.SEVERE, e.toString(), e);
		}
	}
}
