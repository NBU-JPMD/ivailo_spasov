import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.net.*;
import java.io.*;

public class Server {
	private boolean running = false;
	private int port = 6969;
	private int buff_size = 1024;
	
	private void HandleAccept(ServerSocketChannel ssc, Selector sel) {
		SocketChannel client = null;
		try {
			client = ssc.accept();
			client.configureBlocking(false);
			client.register(sel, SelectionKey.OP_READ, null);
			System.out.println("NEW CLIENT " + client.getRemoteAddress());
		}catch (IOException ex) {
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
			if(sel != null)
				sel.close();
		} catch (IOException ex){
			ex.printStackTrace();
		}
	}
	
	private void HandleRead(SocketChannel cl) {
		ByteBuffer buffer = ByteBuffer.allocate(buff_size);
		try {
			if(cl.read(buffer) != -1) {
				System.out.println("MESSAGE FROM CLIENT " + cl.getRemoteAddress());
				buffer.flip();
				cl.write(buffer);
			} else {
				System.out.println("DISCONNECT CLIENT " + cl.getRemoteAddress());
				CloseSelectableChannel(cl);
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
			ServerSocket server_socket = null;
			server_socket = server_socket_chanel.socket();
			server_socket.bind(new InetSocketAddress(port));
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
							HandleRead((SocketChannel)ky.channel());
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