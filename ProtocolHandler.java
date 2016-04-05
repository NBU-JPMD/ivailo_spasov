import java.util.ArrayList;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

class ErrorCmd implements IProtocolCmd {
	public boolean onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) {
		System.out.println(msg);
		return false;
	}

	public String[] getFilters() {
		return new String[]{"error"};
	}
}

public class ProtocolHandler {
	private ArrayList<IProtocolCmd> cmdList = new ArrayList<>();
	private ExecutorService fixedPool;

	public ProtocolHandler(int nThreads) {
		fixedPool = Executors.newFixedThreadPool(nThreads);
		registerCommand(new ErrorCmd());
	}

	public void registerCommand(IProtocolCmd cmd) {
		cmdList.add(cmd);
	}

	public void handleMsg(String type, ISMsg msg, ChannelHelper helper, Object data) {
		if(type != null) {
			Runnable worker = new ProtocolWorkerThread(cmdList, type, msg, helper, data);
			fixedPool.execute(worker);
		}
	}

	void shutdown() {
		fixedPool.shutdown();
	}
}

class ProtocolWorkerThread implements Runnable {
	private ArrayList<IProtocolCmd> cmdList;
	private String type;
	private ISMsg msg;
	private ChannelHelper helper;
	private Object data;

	public ProtocolWorkerThread(ArrayList<IProtocolCmd> cmdList, String type, ISMsg msg, ChannelHelper helper, Object data) {
		this.cmdList = cmdList;
		this.type = type;
		this.msg = msg;
		this.helper = helper;
		this.data = data;
	}

	@Override
	public void run() {
		try {
			boolean isCmdFound = false;
			for(IProtocolCmd cmd : cmdList) {
				for(String filter : cmd.getFilters()) {
					if(type.equals(filter)) {
						isCmdFound = true;
						if(!cmd.onCommand(type, msg, helper, data)) {
							break;
						}
					}
				}
			}
			if(isCmdFound == false) {
				msg = new ISMsg();
				msg.setRespCode(101);
				msg.addKey("type", "error");
				msg.addKey("msg", "Unsupported message type.");
				try {
					helper.getWriter().write(msg);
				} catch (IOException e) {
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}