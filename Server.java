import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.net.*;
import java.io.*;

public class Server {
	private boolean running = false;
	private int port = 6969;
	
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
		 throws IOException, IllegalArgumentException {
		ISMsg msg;
		try {
			msg = pr.getMsg();
			String type = (String)msg.getData("type");
			switch(type) {
				case "echo":
					pr.write(cl);
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
		} catch (IOException ex) {
			ex.printStackTrace();
			CloseSelectableChannel(cl);
		} catch (IllegalArgumentException ise) {
			ise.printStackTrace();
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
			running = false;
		}
	}

	public static void main(String [] args) {
		Server srv = new Server();
		srv.Start();
	}
}