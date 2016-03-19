import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.net.*;
import java.io.*;

public class Server {
	private boolean running = false;
	private int port = 6969;
	private Selector selector = null;
	private static ISUserManager user_manager = ISUserManager.getInstance();

	public void Stop() {
		running = false;
		System.out.println("Stoping server...");
		if(selector != null) {
			selector.wakeup();
		}
	}
	
	private void HandleAccept(ServerSocketChannel ssc, Selector sel) {
		SocketChannel client = null;
		try {
			client = ssc.accept();
			client.configureBlocking(false);
			client.register(sel, SelectionKey.OP_READ, new ISProtocol());
			System.out.println("NEW CLIENT " + client.getRemoteAddress());
		} catch (IOException ex) {
			ex.printStackTrace();
			CloseSelectableChannel(client);
		}
	}
	
	private void CloseSelectableChannel(SelectableChannel ch) {
		try {
			if(ch != null) {
				ch.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private void CloseSelector(Selector sel) {
		try {
			if(sel != null) {
				for(SelectionKey ky : sel.keys()) {
					CloseSelectableChannel(ky.channel());
				}
				sel.close();
			}
		} catch (IOException ex){
			ex.printStackTrace();
		}
	}

	ISMsg CreateErrorMsg(int rsp_code, String message) {
		ISMsg msg = new ISMsg();
		msg.setRespCode(rsp_code);
		msg.addKey("type", "error");
		msg.addKey("msg", message);
		return msg;
	}

	void SendErrorMsg(SocketChannel cl, ISProtocol pr, int rsp_code, String message)
		 throws IOException, IllegalArgumentException {
		pr.setMsg(CreateErrorMsg(rsp_code, message));
		pr.write(cl);
	}

	private void HandleMsg(SocketChannel cl, ISProtocol pr)
		 throws IOException, IllegalArgumentException, Exception {
		ISMsg msg;
		try {
			msg = pr.getMsg();
			String type = (String)msg.getData("type");
			switch(type) {
				case "echo":
					pr.write(cl);
					break;
				case "authenticate":
					String user = (String)msg.getData("user");
					String password = (String)msg.getData("password");
					if(user_manager.isUserValid(user, password)) {
						pr.setMsg(new ISMsg());
						pr.write(cl);
					} else {
						SendErrorMsg(cl, pr, 200, "Invalid username or password");
						throw new Exception("authentication failure");
					}
					break;
				default:
					SendErrorMsg(cl, pr, 101, "Not implemented");
			}
		} finally {
			pr.reset();
		}
	}

	private void HandleRead(SelectionKey ky) {
		SocketChannel cl = (SocketChannel)ky.channel();
		ISProtocol pr = (ISProtocol)ky.attachment();

		try {
			ParseState state = pr.read(cl);
			if(state == ParseState.READ_DONE) {
				HandleMsg(cl, pr);
			} else if (state == ParseState.READ_ERROR) {
				SendErrorMsg(cl, pr, 102, "ParseState.READ_ERROR");
				pr.reset();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			CloseSelectableChannel(cl);
		} catch (IllegalArgumentException ise) {
			ise.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			CloseSelectableChannel(cl);
		}
	}
	
	public void Start(){
		ServerSocketChannel server_socket_chanel = null;

		if (running == true) {
			Stop();
		}

		running = true;
		System.out.println("Starting...");
		try {
			server_socket_chanel = ServerSocketChannel.open();
			server_socket_chanel.socket().bind(new InetSocketAddress(port));
			server_socket_chanel.configureBlocking(false);
			
			selector = Selector.open();
			server_socket_chanel.register(selector, SelectionKey.OP_ACCEPT, null);
			
			while(running) {
				if (selector.select() > 0) {
					Set<SelectionKey> selectedKeys = selector.selectedKeys();
					Iterator<SelectionKey> iter = selectedKeys.iterator();
					while (iter.hasNext()) {
						SelectionKey ky = iter.next();
						if (ky.isAcceptable()) {
							HandleAccept(server_socket_chanel, selector);
						} else if (ky.isReadable()) {
							HandleRead(ky);
						}
						iter.remove();
					}
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			CloseSelectableChannel(server_socket_chanel);
			CloseSelector(selector);
			selector = null;
			running = false;
		}
	}

	private static void addUser(BufferedReader br, ISUserManager user_manager) {
		try {
			System.out.println("Enter new user:");
			String user = br.readLine();
			System.out.println("password:");
			String password = br.readLine();
			user_manager.addUser(user, password);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void delUser(BufferedReader br, ISUserManager user_manager) {
		try {
			System.out.println("Enter user to delete:");
			String user = br.readLine();
			user_manager.delUser(user);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String [] args) {
		ServerThread st = new ServerThread();
		st.start();

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String line;
		try {
			outerloop:
			while((line = br.readLine()) != null) {
				line = line.replaceAll("\\s+","");
				line = line.toLowerCase();
				switch(line) {
					case "stop":
						st.Stop();
						break outerloop;
					case "add":
						addUser(br, user_manager);
						user_manager.save();
						break;
					case "list":
						System.out.println(user_manager.toString());
						break;
					case "del":
						delUser(br, user_manager);
						user_manager.save();
						break;
					default:
						System.out.println("Unknown command");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(br != null)
					br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

class ServerThread extends Thread {
	private Server srv = new Server();

	public void run() {
		srv.Start();
	}

	public void Stop() {
		srv.Stop();
	}
}