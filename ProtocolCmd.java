import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

class AuthCmd implements IProtocolCmd {
	private UserManager userManager = UserManager.getInstance();
	private HashMap<ChannelHelper, String> users = null;

	public AuthCmd(HashMap<ChannelHelper, String> users) {
		this.users = users;
	}

	public boolean onCommand(String cmd, ISMsg msg, ChannelHelper helper) throws IOException {
		String user = (String)msg.getData("user");
		String password = (String)msg.getData("password");
		msg = new ISMsg();
		if(users.get(helper) != null) {
			msg.addKey("type", "authenticateRsp");
			msg.addKey("user", user);
			msg.addKey("msg", "user is already authenticated");
			msg.setRespCode(201);
			helper.getWriter().write(msg);
			return false;
		}
		if(userManager.isUserValid(user, password)) {
			if(userManager.connectUser(user)) {
				msg.addKey("type", "authenticateRsp");
				msg.addKey("user", user);
				users.put(helper, user);
			} else {
				msg.addKey("type", "authenticateRsp");
				msg.addKey("user", user);
				msg.addKey("msg", "user already connected");
				msg.setRespCode(202);
			}
		} else {
			msg.addKey("type", "authenticateRsp");
			msg.addKey("user", user);
			msg.addKey("msg", "wrong username or password");
			msg.setRespCode(203);
		}
		helper.getWriter().write(msg);
		return false;
	}

	public String[] getFilters() {
		return new String[]{"authenticate"};
	}
}

class AuthRspCmd implements IProtocolCmd {
	public boolean onCommand(String cmd, ISMsg msg, ChannelHelper helper) throws IOException {
		if(msg.getRespCode() == 0) {
			System.out.println("user authenticated");
		} else {
			System.out.println(msg);
		}
		return false;
	}

	public String[] getFilters() {
		return new String[]{"authenticateRsp"};
	}
}

class EchoCmd implements IProtocolCmd {
	public boolean onCommand(String cmd, ISMsg msg, ChannelHelper helper) throws IOException {
		if (cmd.equals("echo")) {
			msg.addKey("type", "echoRsp");
			helper.getWriter().write(msg);
		} else if (cmd.equals("echoRsp")){
			System.out.println(msg);
		}
		return false;
	}

	public String[] getFilters() {
		return new String[]{"echo", "echoRsp"};
	}
}

class ListCmd implements IProtocolCmd {
	private HashMap<ChannelHelper, String> users = null;

	public ListCmd(HashMap<ChannelHelper, String> users) {
		this.users = users;
	}

	public boolean onCommand(String cmd, ISMsg msg, ChannelHelper helper) throws IOException {
		msg = new ISMsg();
		if(users.get(helper) != null) {
			File folder = new File("upload/");
			File[] listOfFiles = folder.listFiles();
			ArrayList<String> filesArray = new ArrayList<String>();

			for (File file : listOfFiles) {
				if (file.isFile()) {
					filesArray.add(file.getName());
				}
			}
			msg.addKey("type", "listRsp");
			msg.addKey("files", filesArray);
		} else {
			msg.addKey("type", "listRsp");
			msg.addKey("msg", "user is not authenticated");
			msg.setRespCode(101);
		}
		helper.getWriter().write(msg);
		return false;
	}

	public String[] getFilters() {
		return new String[]{"list"};
	}
}

class ListRspCmd implements IProtocolCmd {
	@SuppressWarnings("unchecked")
	public boolean onCommand(String cmd, ISMsg msg, ChannelHelper helper) throws IOException {
		if(msg.getRespCode() == 0) {
			System.out.println("Server files:");
			ArrayList<String> filesArray = (ArrayList<String>)msg.getData("files");
			for(String file : filesArray) {
				System.out.println(file);
			}
		} else {
			System.out.println(msg);
		}
		return false;
	}

	public String[] getFilters() {
		return new String[]{"listRsp"};
	}
}