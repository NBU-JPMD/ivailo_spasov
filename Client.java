import java.nio.*;
import java.nio.channels.*;
import java.io.*;
import java.net.*;

public class Client {
	private final static String remoteHost = "localhost";
	private final static int remotePort = 6969;

	private SocketChannel socketChannel = null;
	private ChannelWriter channelWriter = null;
	private ChannelReader channelReader = null;
	private Thread recvThread = null;

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
		channelWriter = new ChannelWriter(socketChannel);
		channelReader = new ChannelReader(socketChannel);
		recvThread = new Thread() {
			public void run() {
				while(true) {
					try {
						System.out.println(channelReader.recv());
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
		channelWriter = null;
		channelReader = null;
	}

	public void write(Object obj) throws IOException {
		channelWriter.write(obj);
	}

	public static void main(String [] args) {
		Client cl = new Client();

		CommandHandler commandHandler = new CommandHandler();
		commandHandler.registerCommand(new ClientCommand(cl));
		commandHandler.registerCommand(new ClientEchoCommand(cl));
		commandHandler.registerCommand(new ClientAuthCommand(cl));
		commandHandler.start();

		cl.stopClient();
	}
}