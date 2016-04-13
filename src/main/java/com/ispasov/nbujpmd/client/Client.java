package com.ispasov.nbujpmd.client;

import com.ispasov.nbujpmd.common.channel.ChannelHelper;
import com.ispasov.nbujpmd.common.protocol.ProtocolHandler;
import com.ispasov.nbujpmd.common.UserState;
import com.ispasov.nbujpmd.common.protocol.cmd.*;
import com.ispasov.nbujpmd.common.command.CommandHandler;
import com.ispasov.nbujpmd.common.protocol.ISMsg;

import java.nio.*;
import java.nio.channels.*;
import java.io.*;
import java.net.*;

public class Client {
	private final static String remoteHost = "localhost";
	private final static int remotePort = 6969;

	private SocketChannel socketChannel = null;
	private ChannelHelper helper = null;
	private Thread recvThread = null;
	private ProtocolHandler protocolHandler = null;
	private UserState userState = null;

	public UserState getUserState() {
		return userState;
	}

	public static String getDefaultHost() {
		return remoteHost;
	}

	public static int getDefaultPort() {
		return remotePort;
	}

	private void closeSelectableChannel(SelectableChannel ch) {
		try {
			if(ch != null) {
				ch.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public synchronized boolean isRunning() {
		return (socketChannel != null);
	}

	public synchronized void startClient(String host, int port) throws IOException {
		socketChannel = SocketChannel.open();
		socketChannel.connect(new InetSocketAddress(host, port));
		helper = new ChannelHelper(socketChannel);
		protocolHandler = new ProtocolHandler(1);
		protocolHandler.registerCommand(new EchoCmd());
		protocolHandler.registerCommand(new AuthRspCmd());
		protocolHandler.registerCommand(new ListRspCmd());
		protocolHandler.registerCommand(new UploadRspCmd());
		protocolHandler.registerCommand(new DownloadRspCmd());
		protocolHandler.registerCommand(new ClientPieceCmd());
		userState = new UserState(helper);

		recvThread = new Thread() {
			public void run() {
				while(true) {
					try {
						for(Object obj : helper.getReader().recv()) {
							ISMsg msg = (ISMsg)obj;
							String type = (String)msg.getData("type");
							if(type != null) {
								if(protocolHandler != null) {
									protocolHandler.handleMsg(type, msg, helper, userState);
								}
							}
						}
					} catch (ClosedByInterruptException cie) {
						break;
					} catch (IOException e) {
						e.printStackTrace();
						stopClient();
						break;
					}
				}
			}
		};
		recvThread.start();
	}

	public synchronized void stopClient() {
		if(recvThread != null) {
			recvThread.interrupt();
			try {
				recvThread.join();
			} catch (InterruptedException e) {
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
}