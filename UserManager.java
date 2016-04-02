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

public class UserManager implements Serializable {
	private static UserManager instance = null;
	private static String user_file = "users.dat";
	private HashMap<String, String> users = new HashMap<>();
	private transient HashSet<String> connected_users = new HashSet<>();
	static final long serialVersionUID = 69;

	protected UserManager() {
	}

	public static UserManager getInstance() {
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

	public void addUser(String user, String password) {
		users.put(user, password);
	}

	public void delUser(String user) {
		users.remove(user);
	}

	public boolean connectUser(String user) {
		if(users.get(user) != null && !connected_users.contains(user)) {
			connected_users.add(user);
			return true;
		}
		return false;
	}

	public void disconnectUser(String user) {
		if(user != null) {
			connected_users.remove(user);
		}
	}

	public boolean isUserValid(String user, String password) {
		String ck_password = users.get(user);
		if(ck_password != null && ck_password.equals(password)) {
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

	public void save() {
		try (FileOutputStream fin = new FileOutputStream(user_file);
			ObjectOutputStream ois = new ObjectOutputStream(fin)) {
			ois.writeObject(this);
		} catch (Exception e) {
			e.printStackTrace();
			instance = new UserManager();
		}
	}
}
