import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.lang.IllegalArgumentException;

class AuthCmd implements IProtocolCmd {
	private UserManager userManager = UserManager.getInstance();

	public boolean onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException {
		String user = (String)msg.getData("user");
		String password = (String)msg.getData("password");
		msg = new ISMsg();
		UserState userState  = (UserState)data;
		synchronized (userState) {
			if(userState.getUser() != null) {
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
					userState.setUser(user);
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
		}
		helper.getWriter().write(msg);
		return false;
	}

	public String[] getFilters() {
		return new String[]{"authenticate"};
	}
}

class AuthRspCmd implements IProtocolCmd {
	public boolean onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException {
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
	public boolean onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException {
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
	public boolean onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException {
		msg = new ISMsg();
		UserState userState  = (UserState)data;
		String user;
		synchronized (userState) {
			user = userState.getUser();
		}

		if(user != null) {
			File folder = new File("upload/");
			ArrayList<String> filesArray = new ArrayList<String>();

			if(folder.exists() && folder.isDirectory()) {
				File[] listOfFiles = folder.listFiles();
				for (File file : listOfFiles) {
					if (file.isFile()) {
						filesArray.add(file.getName());
					}
				}
			} else {
				folder.mkdirs();
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
	public boolean onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException {
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

class UploadCmd implements IProtocolCmd {
	public boolean onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException {
		UserState userState  = (UserState)data;
		String user;
		synchronized (userState) {
			user = userState.getUser();
			if(user != null) {
				ReceiveFile receiveFile = userState.getReceiveFile();
				if (receiveFile != null) {
					msg = new ISMsg();
					msg.addKey("type", "uploadRsp");
					msg.addKey("msg", "file transfer is already in progress");
					msg.setRespCode(303);
					helper.getWriter().write(msg);
					return false;
				}

				String file = null;
				long pieces = -1;
				try {
					file = (String)msg.getData("file");
					pieces = (long)msg.getData("pieces");
				} catch (Exception e) {
				}

				try {
					receiveFile = new ReceiveFile(file, pieces);
					userState.setReceiveFile(receiveFile);
					msg = new ISMsg();
					msg.addKey("type", "uploadRsp");
					msg.addKey("file", file);
				} catch (IllegalArgumentException iae) {
					msg = new ISMsg();
					msg.addKey("type", "uploadRsp");
					msg.addKey("msg", "missing file information");
					msg.setRespCode(302);
				} catch (FileAlreadyExistsException fae) {
					msg = new ISMsg();
					msg.addKey("type", "uploadRsp");
					msg.addKey("msg", "file already exist");
					msg.setRespCode(301);
				}
			} else {
				msg = new ISMsg();
				msg.addKey("type", "uploadRsp");
				msg.addKey("msg", "user is not authenticated");
				msg.setRespCode(304);
			}
		}
		helper.getWriter().write(msg);
		return false;
	}

	public String[] getFilters() {
		return new String[]{"upload"};
	}
}

class UploadRspCmd implements IProtocolCmd {
	private Client client;

	public UploadRspCmd(Client client) {
		this.client = client;
	}

	public boolean onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException {
		SendFile sendFIle = client.getSendFile();
		if(msg.getRespCode() == 0) {
			if(sendFIle != null) {
				msg = sendFIle.getNextMsg();
				helper.getWriter().write(msg);
				if(sendFIle.isFileSend()) {
					System.out.println("sending file done");
					sendFIle.close();
					client.setSendFile(null);
				}
			}
		} else {
			if(sendFIle != null) {
				sendFIle.close();
				client.setSendFile(null);
			}
			System.out.println(msg);
		}
		return false;
	}

	public String[] getFilters() {
		return new String[]{"uploadRsp", "pieceRsp"};
	}
}

class PieceCmd implements IProtocolCmd {
	public boolean onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException {
		UserState userState  = (UserState)data;
		String user;

		synchronized (userState) {
			user = userState.getUser();
			if(user != null) {
				ReceiveFile receiveFile = userState.getReceiveFile();
				if (receiveFile == null) {
					msg = new ISMsg();
					msg.addKey("type", "pieceRsp");
					msg.addKey("msg", "file transfer is not in progress");
					msg.setRespCode(401);
					helper.getWriter().write(msg);
					return false;
				}

				long piece = 0;
				byte[] byteData = null;
				try {
					piece = (long)msg.getData("piece");
					byteData = (byte[])msg.getData("data");
				} catch (Exception e) {
				}

				try {
					receiveFile.write(byteData, piece);
					msg = new ISMsg();
					msg.addKey("type", "pieceRsp");

					if(receiveFile.isFileReceived()) {
						receiveFile.close();
						userState.setReceiveFile(null);
					}
				} catch (IllegalArgumentException iae) {
					msg = new ISMsg();
					msg.addKey("type", "pieceRsp");
					msg.addKey("msg", "wrong piece number");
					msg.setRespCode(402);
				} catch (IOException ioe) {
					msg = new ISMsg();
					msg.addKey("type", "pieceRsp");
					msg.addKey("msg", "write file exception");
					msg.setRespCode(403);
				}
			} else {
				msg = new ISMsg();
				msg.addKey("type", "pieceRsp");
				msg.addKey("msg", "user is not authenticated");
				msg.setRespCode(404);
			}
		}
		helper.getWriter().write(msg);
		return false;
	}
	public String[] getFilters() {
		return new String[]{"piece"};
	}
}

class DownloadCmd implements IProtocolCmd {
	public boolean onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException {
		UserState userState  = (UserState)data;
		String user;

		synchronized (userState) {
			user = userState.getUser();
			if(user != null) {
				SendFile sendFile = userState.getSendFile();
				if (sendFile != null) {
					msg = new ISMsg();
					msg.addKey("type", "downloadRsp");
					msg.addKey("msg", "file transfer is in progress");
					msg.setRespCode(501);
					helper.getWriter().write(msg);
					return false;
				}
				String file = null;
				try {
					file = (String)msg.getData("file");
				} catch (Exception e) {
				}
				try {
					sendFile = new SendFile("upload/" + file, "downloadRsp");
					userState.setSendFile(sendFile);
					msg = sendFile.getUploadMsg();
				} catch (IOException ioe) {
					msg = new ISMsg();
					msg.addKey("type", "downloadRsp");
					msg.addKey("msg", "missing file");
					msg.setRespCode(502);
				}
			} else {
				msg = new ISMsg();
				msg.addKey("type", "downloadRsp");
				msg.addKey("msg", "user is not authenticated");
				msg.setRespCode(503);
			}
		}
		helper.getWriter().write(msg);
		return false;
	}

	public String[] getFilters() {
		return new String[]{"download"};
	}
}

class DownloadRspCmd implements IProtocolCmd {
	private Client client;

	public DownloadRspCmd(Client client) {
		this.client = client;
	}

	public boolean onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException {
		ReceiveFile receiveFile = client.getReceiveFile();
		if(msg.getRespCode() == 0) {
			if(receiveFile != null) {
				long pieces = -1;
				try {
					pieces = (long)msg.getData("pieces");
				}catch (Exception e) {
				}
				msg = new ISMsg();
				msg.addKey("type", "reqPiece");
				if(pieces < 0) {
					receiveFile.close();
					receiveFile.deleteFile();
					client.setReceiveFile(null);
					msg.setRespCode(600);
					System.out.println("Unable to receive file, wrong pieces count.");
					return false;
				}
				receiveFile.setPieces(pieces);
				helper.getWriter().write(msg);
			}
		} else {
			if(receiveFile != null) {
				receiveFile.close();
				receiveFile.deleteFile();
				client.setReceiveFile(null);
			}
			System.out.println(msg);
		}
		return false;
	}

	public String[] getFilters() {
		return new String[]{"downloadRsp"};
	}
}

class ReqPieceCmd implements IProtocolCmd {
	public boolean onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException {
		UserState userState  = (UserState)data;
		String user;

		synchronized (userState) {
			user = userState.getUser();
			SendFile sendFile = userState.getSendFile();
			if(user != null) {
				if (sendFile == null) {
					msg = new ISMsg();
					msg.addKey("type", "piece");
					msg.addKey("msg", "no file transfer is in progress");
					msg.setRespCode(601);
					helper.getWriter().write(msg);
					return false;
				}
				if(msg.getRespCode() == 0) {
					msg = sendFile.getNextMsg();
					if(sendFile.isFileSend()) {
						sendFile.close();
						userState.setSendFile(null);
					}
				} else {
					sendFile.close();
					userState.setSendFile(null);
					return false;
				}
			} else {
				msg = new ISMsg();
				msg.addKey("type", "piece");
				msg.addKey("msg", "user is not authenticated");
				msg.setRespCode(603);
				if(sendFile != null) {
					sendFile.close();
					userState.setSendFile(null);
				}
			}
		}
		helper.getWriter().write(msg);
		return false;
	}

	public String[] getFilters() {
		return new String[]{"reqPiece"};
	}
}

class ClientPieceCmd implements IProtocolCmd {
	private Client client;

	public ClientPieceCmd(Client client) {
		this.client = client;
	}

	public boolean onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException {
		ReceiveFile receiveFile = client.getReceiveFile();
		if(msg.getRespCode() == 0) {
			if(receiveFile != null) {
				long piece = 0;
				byte[] byteData = null;
				try {
					piece = (long)msg.getData("piece");
					byteData = (byte[])msg.getData("data");
				} catch (Exception e) {
				}
				try {
					receiveFile.write(byteData, piece);

					if(receiveFile.isFileReceived()) {
						receiveFile.close();
						client.setReceiveFile(null);
						System.out.println("Receive file done");
						return false;
					}
					msg = new ISMsg();
					msg.addKey("type", "reqPiece");
				} catch (IllegalArgumentException iae) {
					msg = new ISMsg();
					msg.addKey("type", "reqPiece");
					msg.addKey("msg", "wrong piece number");
					msg.setRespCode(701);
				} catch (IOException ioe) {
					msg = new ISMsg();
					msg.addKey("type", "reqPiece");
					msg.addKey("msg", "write file exception");
					msg.setRespCode(702);
				}
			} else {
				msg = new ISMsg();
				msg.addKey("type", "reqPiece");
				msg.addKey("msg", "file transfer is not in progress");
				msg.setRespCode(703);
			}
		} else {
			if(receiveFile != null) {
				receiveFile.close();
				receiveFile.deleteFile();
				client.setReceiveFile(null);
			}
			System.out.println(msg);
		}
		helper.getWriter().write(msg);
		return false;
	}

	public String[] getFilters() {
		return new String[]{"piece"};
	}
}