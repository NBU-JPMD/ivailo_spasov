import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.net.*;
import java.io.*;

class ChannelHealper {
	private ChannelReader channelReader = null;
	private ChannelWriter channelWriter = null;
	public ChannelHealper(SocketChannel client) {
		channelReader = new ChannelReader(client);
		channelWriter = new ChannelWriter(client);
	}

	public ChannelReader getReader() {
		return channelReader;
	}

	public ChannelWriter getWriter() {
		return channelWriter;
	}
}

public class Server {
	private boolean running = false;
	private int port = 6969;
	
	private void HandleAccept(ServerSocketChannel ssc, Selector sel) {
		SocketChannel client = null;
		try {
			client = ssc.accept();
			client.configureBlocking(false);
			client.register(sel, SelectionKey.OP_READ, new ChannelHealper(client));
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
	
	private void HandleRead(SocketChannel cl, ChannelHealper helper) {
		try {
			for(Object obj : helper.getReader().recv()) {
				ISMsg msg = (ISMsg)obj;
				String type = (String)msg.getData("type");
				if(type != null) {
					switch(type) {
						case "echo":
							helper.getWriter().write(msg);
							break;
						case "authenticate":
							msg = new ISMsg();
							msg.addKey("type", "authenticated");
							helper.getWriter().write(msg);
							break;
						default:
							msg = new ISMsg();
							msg.setRespCode(101);
							msg.addKey("type", "error");
							msg.addKey("msg", "Unsupported message type.");
							helper.getWriter().write(msg);
							break;
					}
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
		}
	}
	
	public void Start(){
		ServerSocketChannel server_socket_chanel = null;
		Selector selector = null;
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
							HandleRead((SocketChannel)ky.channel(), (ChannelHealper)ky.attachment());
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
			running = false;
		}
	}
	
	public static void main(String [] args) {
		Server srv = new Server();
		srv.Start();
	}
}