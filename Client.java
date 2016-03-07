import java.io.*;
import java.net.*;

public class Client {
	private String remote_host = "localhost";
	private int remote_port = 6969;
	private int buff_size = 1024;
	
	private void CloseSocket(Socket soc) {
		try {
			if(soc != null) {
				soc.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private void Start() {
		Socket client = null;
		try {
			client = new Socket(remote_host, remote_port);
			InputStream cl_is = client.getInputStream();
			OutputStream cl_os = client.getOutputStream();
			
			byte[] buff = new byte[buff_size];
			int read = 0;
			
			while((read = System.in.read(buff)) != -1) {
				cl_os.write(buff, 0, read);
				cl_os.flush();
				if((read = cl_is.read(buff)) != -1) {
					System.out.write(buff, 0, read);
					System.out.flush();
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			CloseSocket(client);
		}
	}
	
	public static void main(String [] args) {
		Client cl = new Client();
		cl.Start();
	}
}