import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
	private String remote_host = "localhost";
	private int remote_port = 6969;
	
	private void CloseSocket(Socket soc) {
		try {
			if(soc != null) {
				soc.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	boolean authenticate(ISProtocol pr, InputStream cl_is, OutputStream cl_os) {
		ParseState status;
		ISMsg msg = new ISMsg();
		msg.addKey("type", "authenticate");
		msg.addKey("user", "test");
		msg.addKey("password", "pass");
		try {
			pr.setMsg(msg);
			pr.write(cl_os);

			pr.reset();

			do {
				status = pr.read(cl_is);
			} while(status != ParseState.READ_DONE && status != ParseState.READ_ERROR);

			if(status == ParseState.READ_DONE) {
				msg = pr.getMsg();
				if(msg.getRespCode() == 0) {
					return true;
				}
			} else {
				System.out.println("RSP ERROR");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private void Start() {
		Socket client = null;
		try {
			client = new Socket(remote_host, remote_port);
			InputStream cl_is = client.getInputStream();
			OutputStream cl_os = client.getOutputStream();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String line;
			ISProtocol pr = new ISProtocol();
			ParseState status;

			if(authenticate(pr, cl_is, cl_os) == true) {
				while((line = br.readLine()) != null) {
					ISMsg msg = new ISMsg();
					msg.addKey("type", "echo");
					msg.addKey("msg", line);

					pr.setMsg(msg);
					pr.write(cl_os);
					pr.reset();

					do {
						status = pr.read(cl_is);
					} while(status != ParseState.READ_DONE && status != ParseState.READ_ERROR);

					if(status == ParseState.READ_DONE) {
						System.out.println(pr.getMsg());
					} else {
						System.out.println("RSP ERROR");
					}
				}
			} else {
				System.out.println("authentication failure");
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