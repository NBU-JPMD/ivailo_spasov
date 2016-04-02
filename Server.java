import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.net.*;
import java.io.*;

public class Server {
	private static final int defaultPort = 6969;

	private boolean running = false;
	private static UserManager userManager = UserManager.getInstance();
	private Thread recvThread = null;
	private ServerSocketChannel server_socket_chanel = null;
	private Selector selector = null;
	private ProtocolHandler protocolHandler = null;
	private HashMap<ChannelHelper, String> users = null;

	public static int getDefaultPort() {
		return defaultPort;
	}

	public boolean isRunning() {
		return running;
	}

	private void HandleAccept(ServerSocketChannel ssc, Selector sel) {
		SocketChannel client = null;
		try {
			client = ssc.accept();
			client.configureBlocking(false);
			client.register(sel, SelectionKey.OP_READ, new ChannelHelper(client));
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

	private void HandleRead(SocketChannel cl, ChannelHelper helper) {
		try {
			for(Object obj : helper.getReader().recv()) {
				ISMsg msg = (ISMsg)obj;
				String type = (String)msg.getData("type");
				if(type != null) {
					protocolHandler.handleMsg(type, msg, helper);
				} else {
					msg = new ISMsg();
					msg.setRespCode(102);
					msg.addKey("type", "error");
					msg.addKey("msg", "Type is missing.");
					helper.getWriter().write(msg);
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			CloseSelectableChannel(cl);
			userManager.disconnectUser(users.get(helper));
			users.remove(helper);
		}
	}
	
	public synchronized void startServer(int port) throws IOException {
		running = true;
		System.out.println("Starting at port " + port);

		server_socket_chanel = ServerSocketChannel.open();
		server_socket_chanel.socket().bind(new InetSocketAddress(port));
		server_socket_chanel.configureBlocking(false);

		selector = Selector.open();
		server_socket_chanel.register(selector, SelectionKey.OP_ACCEPT, null);

		users = new HashMap<>();

		protocolHandler = new ProtocolHandler();
		protocolHandler.registerCommand(new EchoCmd());
		protocolHandler.registerCommand(new AuthCmd(users));
		protocolHandler.registerCommand(new ListCmd(users));

		recvThread = new Thread() {
			public void run() {
				try {
					while(running) {
						if (selector.select() > 0) {
							Set<SelectionKey> selectedKeys = selector.selectedKeys();
							Iterator<SelectionKey> iter = selectedKeys.iterator();
							while (iter.hasNext()) {
								SelectionKey ky = iter.next();
								if (ky.isAcceptable()) {
									HandleAccept(server_socket_chanel, selector);
								} else if (ky.isReadable()) {
									HandleRead((SocketChannel)ky.channel(), (ChannelHelper)ky.attachment());
								}
								iter.remove();
							}
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				} finally {
					CloseSelectableChannel(server_socket_chanel);
					CloseSelector(selector);
					selector = null;
					server_socket_chanel = null;
					running = false;
				}
			}
		};
		recvThread.start();
	}

	public synchronized void stopServer() {
		running = false;
		if(selector != null) {
			selector.wakeup();
		}
		try {
			if(recvThread != null) {
				recvThread.join();
			}
		} catch (InterruptedException e) {
		} finally {
			recvThread = null;
		}
	}

	public static void main(String [] args) {
		Server srv = new Server();

		CommandHandler commandHandler = new CommandHandler();
		commandHandler.registerCommand(new ServerCommand(srv));
		commandHandler.registerCommand(new ServerUsersCommand(userManager));
		commandHandler.start();

		srv.stopServer();
	}
}
