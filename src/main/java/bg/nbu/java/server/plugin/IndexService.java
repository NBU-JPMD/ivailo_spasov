package bg.nbu.java.server.plugin;

public class IndexService {
	static {
		System.loadLibrary("IndexService");
	}

	public native String[] index(String text);
}