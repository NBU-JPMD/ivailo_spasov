package com.ispasov.nbujpmd.client;

import com.ispasov.nbujpmd.common.UserState;
import com.ispasov.nbujpmd.common.channel.ChannelHelper;
import com.ispasov.nbujpmd.common.command.CommandHandler;
import com.ispasov.nbujpmd.common.protocol.ISMsg;
import com.ispasov.nbujpmd.common.protocol.cmd.*;
import com.ispasov.nbujpmd.common.protocol.IProtocolHandler;
import com.ispasov.nbujpmd.common.protocol.SingleProtocolHandler;


import java.io.*;
import java.net.*;
import java.util.logging.*;
import java.nio.channels.*;

public class Client {
	private static final Logger LOG = Logger.getLogger(Client.class.getName());
	private static final String REMOTEHOST = "localhost";
	private static final int REMOTEPORT = 6969;

	private SocketChannel socketChannel = null;
	private ChannelHelper helper = null;
	private Thread recvThread = null;
	private IProtocolHandler protocolHandler = null;
	private UserState userState = null;

	public UserState getUserState() {
		return userState;
	}

	public static String getDefaultHost() {
		return REMOTEHOST;
	}

	public static int getDefaultPort() {
		return REMOTEPORT;
	}

	private void closeSelectableChannel(SelectableChannel ch) {
		try {
			if(ch != null) {
				ch.close();
			}
		} catch (IOException ioe) {
			LOG.log(Level.SEVERE, ioe.toString(), ioe);
		}
	}

	public synchronized boolean isRunning() {
		return (socketChannel != null);
	}

	public synchronized void startClient(String host, int port) throws IOException {
		socketChannel = SocketChannel.open();
		socketChannel.connect(new InetSocketAddress(host, port));
		helper = new ChannelHelper(socketChannel);
		protocolHandler = new SingleProtocolHandler();
		protocolHandler.registerCommand(new EchoCmd());
		protocolHandler.registerCommand(new AuthRspCmd());
		protocolHandler.registerCommand(new ListRspCmd());
		protocolHandler.registerCommand(new UploadRspCmd());
		protocolHandler.registerCommand(new DownloadRspCmd());
		protocolHandler.registerCommand(new ClientPieceCmd());
		userState = new UserState(helper);

		recvThread = new RecvThread();
		recvThread.start();
	}

	public synchronized void stopClient() {
		if(recvThread != null) {
			recvThread.interrupt();
			try {
				recvThread.join();
			} catch (InterruptedException ie) {
			} finally {
				recvThread = null;
			}
		}
		if (socketChannel != null) {
			closeSelectableChannel(socketChannel);
			socketChannel = null;
		}
		if(protocolHandler != null) {
			protocolHandler.shutdown();
			protocolHandler = null;
		}
		if(userState != null) {
			userState.close();
			userState = null;
		}
	}

	public static void main(String [] args) {
		System.out.println("NBUJPMD Client. Type \"help\" for command list");
		Client cl = new Client();

		CommandHandler commandHandler = new CommandHandler();
		commandHandler.registerCommand(new ClientCommand(cl));
		commandHandler.registerCommand(new ClientEchoCommand(cl));
		commandHandler.registerCommand(new ClientAuthCommand(cl));
		commandHandler.registerCommand(new ClientListCommand(cl));
		commandHandler.registerCommand(new ClientUploadCommand(cl));
		commandHandler.registerCommand(new ClientDownloadCommand(cl));
		commandHandler.start();

		cl.stopClient();
	}

	private class RecvThread extends Thread {
		@Override
		public void run() {
			while(true) {
				try {
					for(Object obj : helper.getReader().recv()) {
						ISMsg msg = (ISMsg)obj;
						String type = (String)msg.getData("type");
						if(type != null && protocolHandler != null) {
							protocolHandler.handleMsg(type, msg, helper, userState);
						}
					}
				} catch (ClosedByInterruptException cie) {
					break;
				} catch (IOException ioe) {
					System.out.println("Disconnected: " + ioe.getMessage());
					stopClient();
					break;
				}
			}
		}
	}
}