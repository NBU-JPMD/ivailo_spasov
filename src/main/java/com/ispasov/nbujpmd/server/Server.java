package com.ispasov.nbujpmd.server;

import com.ispasov.nbujpmd.common.UserState;
import com.ispasov.nbujpmd.common.UserManager;
import com.ispasov.nbujpmd.common.SearchManager;
import com.ispasov.nbujpmd.common.channel.ChannelHelper;
import com.ispasov.nbujpmd.common.channel.ChannelReader;
import com.ispasov.nbujpmd.common.command.CommandHandler;
import com.ispasov.nbujpmd.common.protocol.ISMsg;
import com.ispasov.nbujpmd.common.protocol.cmd.*;
import com.ispasov.nbujpmd.common.protocol.IProtocolHandler;
import com.ispasov.nbujpmd.common.protocol.ExecutorProtocolHandler;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import java.nio.channels.*;
import java.rmi.*;
import java.rmi.server.*;

public class Server {
	private static final Logger LOG = Logger.getLogger(Server.class.getName());
	private static final int DEFAULTPORT = 6969;
	private static final int EXECUTORTHREADS = 1000;

	private final UserManager userManager = UserManager.getInstance();
	private boolean running = false;
	private Thread recvThread = null;
	private ServerSocketChannel server_socket_chanel = null;
	private Selector selector = null;
	private IProtocolHandler protocolHandler = null;

	private ServerRmiLogin rmiLogin = null;

	public static int getDefaultPort() {
		return DEFAULTPORT;
	}

	public boolean isRunning() {
		return running;
	}

	public void disconnectUser(String user) {
		if(selector == null) {
			return;
		}

		for(SelectionKey ky : selector.keys()) {
			UserState userState = (UserState)ky.attachment();
			synchronized (userState) {
				if (user.equals(userState.getUser())) {
					CloseSelectableChannel(ky.channel());
					userManager.disconnectUser(userState.getUser());
					userState.close();
					break;
				}
			}
		}
	}

	private void HandleAccept(ServerSocketChannel ssc, Selector sel) {
		SocketChannel client = null;
		try {
			client = ssc.accept();
			client.configureBlocking(false);
			client.register(sel, SelectionKey.OP_READ, new UserState(new ChannelHelper(client)));
			System.out.println("NEW CLIENT " + client.getRemoteAddress());
		} catch (IOException ioe) {
			CloseSelectableChannel(client);
			LOG.log(Level.SEVERE, ioe.toString(), ioe);
		}
	}

	private void CloseSelectableChannel(SelectableChannel ch) {
		try {
			if(ch != null) {
				ch.close();
			}
		} catch (IOException ioe) {
			LOG.log(Level.SEVERE, ioe.toString(), ioe);
		}
	}

	private void CloseSelector(Selector sel) {
		try {
			if(sel != null) {
				sel.keys().forEach(ky -> CloseSelectableChannel(ky.channel()));
				sel.close();
			}
		} catch (IOException ioe){
			LOG.log(Level.SEVERE, ioe.toString(), ioe);
		}
	}

	private void HandleRead(SocketChannel cl, UserState userState) {
		try {
			ChannelHelper helper = userState.getChannelHelper();
			ChannelReader reader = helper.getReader();
			List<Object> objList = reader.recv();
			if(objList != null) {
				for(Object obj : objList) {
					ISMsg msg = (ISMsg)obj;
					String type = (String)msg.getData("type");
					if(type != null) {
						protocolHandler.handleMsg(type, msg, helper, userState);
					} else {
						msg = new ISMsg();
						msg.setRespCode(102);
						msg.addKey("type", "error");
						msg.addKey("msg", "Type is missing.");
						helper.getWriter().write(msg);
					}
				}
			}
		} catch (IOException ex) {
			System.out.println("DISCONNECT " + userState.getUser() + " (" + ex.getMessage() + ").");
			CloseSelectableChannel(cl);
			synchronized (userState) {
				userManager.disconnectUser(userState.getUser());
				userState.close();
			}
		}
	}

	private void registerRMI() {
		try {
			rmiLogin = new ServerRmiLogin(this);
			Naming.rebind("rmi://0.0.0.0/ServerRmiLogin", rmiLogin);
		} catch (MalformedURLException mue) {
			LOG.log(Level.SEVERE, mue.toString(), mue);
		} catch (RemoteException re) {
			LOG.log(Level.SEVERE, re.toString(), re);
		}
	}

	private void unregisterRMI() {
		try {
			if(Naming.lookup("rmi://0.0.0.0/ServerRmiLogin") != null) {
				Naming.unbind("rmi://0.0.0.0/ServerRmiLogin");
			}
		} catch (Exception e) {
		}
		try {
			if(rmiLogin != null) {
				UnicastRemoteObject.unexportObject(rmiLogin, true);
				rmiLogin = null;
			}
		} catch (NoSuchObjectException nso) {
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

		protocolHandler = new ExecutorProtocolHandler(EXECUTORTHREADS);
		protocolHandler.registerCommand(new EchoCmd());
		protocolHandler.registerCommand(new AuthCmd());
		protocolHandler.registerCommand(new ListCmd());
		protocolHandler.registerCommand(new UploadCmd());
		protocolHandler.registerCommand(new PieceCmd());
		protocolHandler.registerCommand(new DownloadCmd());
		protocolHandler.registerCommand(new ReqPieceCmd());
		protocolHandler.registerCommand(new SearchCmd());

		recvThread = new RecvThread();
		recvThread.start();
		registerRMI();
	}

	public synchronized void stopServer() {
		running = false;
		unregisterRMI();
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
		if(protocolHandler != null) {
			protocolHandler.shutdown();
			protocolHandler = null;
		}
	}

	public void loadPlugins() {
		try {
			SearchManager seachManager = SearchManager.getInstance();
			seachManager.loadPlugin("bg.nbu.java.server.plugin.IndexService");
			System.out.println("bg.nbu.java.server.plugin.IndexService plugin loaded.");
		} catch (Exception | UnsatisfiedLinkError e){
		}
	}

	public static void main(String [] args) {
		System.out.println("NBUJPMD Server. Type \"help\" for command list");
		Server srv = new Server();
		srv.loadPlugins();

		CommandHandler commandHandler = new CommandHandler();
		commandHandler.registerCommand(new ServerCommand(srv));
		commandHandler.registerCommand(new ServerUsersCommand());
		commandHandler.start();

		srv.stopServer();
		System.exit(0);
	}

	private class RecvThread extends Thread {
			@Override
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
									HandleRead((SocketChannel)ky.channel(), (UserState)ky.attachment());
								}
								iter.remove();
							}
						}
					}
				} catch (Exception ex) {
				} finally {
					CloseSelectableChannel(server_socket_chanel);
					CloseSelector(selector);
					selector = null;
					server_socket_chanel = null;
					running = false;
				}
			}
	}
}
