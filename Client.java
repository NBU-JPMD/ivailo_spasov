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

	private synchronized void startClient() throws IOException {
		socketChannel = SocketChannel.open();
		socketChannel.connect(new InetSocketAddress(remoteHost, remotePort));
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

	private synchronized void stopClient() {
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
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			cl.startClient();
		} catch (IOException ioe){
			ioe.printStackTrace();
			cl.stopClient();
		}
		try {
			String command;
			outerloop:
			while((command = br.readLine()) != null) {
				command = command.replaceAll("\\s+", "").toLowerCase();
				switch(command) {
					case "echo":
						if(cl.isRunning()) {
							System.out.println("Enter text:");
							String echoMsg = br.readLine();
							ISMsg msg = new ISMsg();
							msg.addKey("type", "echo");
							msg.addKey("text", echoMsg);
							cl.write(msg);
						} else {
							System.out.println("Client is not running.");
						}
						break;
					case "start":
						if(cl.isRunning()) {
							System.out.println("Client is already running.");
						} else {
							try {
								cl.startClient();
							} catch (IOException ioe){
								ioe.printStackTrace();
								cl.stopClient();
							}
						}
						break;
					case "stop":
						if(cl.isRunning()) {
							cl.stopClient();
						} else {
							System.out.println("Client is not running.");
						}
						break;
					case "exit":
						if(cl.isRunning()) {
							cl.stopClient();
						}
						break outerloop;
					case "status":
						if(cl.isRunning()) {
							System.out.println("Client is running.");
						} else {
							System.out.println("Client is not running.");
						}
						break;
					case "authenticate":
						if(cl.isRunning()) {
							System.out.println("Enter user:");
							String user = br.readLine();
							System.out.println("Enter password:");
							String password = br.readLine();
							ISMsg msg = new ISMsg();
							msg.addKey("type", "authenticate");
							msg.addKey("user", user);
							msg.addKey("password", password);
							cl.write(msg);
						} else {
							System.out.println("Client is not running.");
						}
						break;
					case "help":
						System.out.println("Supported commands: authenticate, echo, start, stop, status, exit");
						break;
					default:
						System.out.println("Unknown command. Type help for more information.");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}