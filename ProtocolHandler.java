import java.util.ArrayList;
import java.io.IOException;

class ErrorCmd implements IProtocolCmd {
	public boolean onCommand(String cmd, ISMsg msg, ChannelHelper helper) throws IOException {
		System.out.println(msg);
		return false;
	}

	public String[] getFilters() {
		return new String[]{"error"};
	}
}

public class ProtocolHandler {
	private ArrayList<IProtocolCmd> cmdList = new ArrayList<>();

	public ProtocolHandler() {
		registerCommand(new ErrorCmd());
	}

	public void registerCommand(IProtocolCmd cmd) {
		cmdList.add(cmd);
	}

	public void handleMsg(String type, ISMsg msg, ChannelHelper helper) throws IOException {
		if(type != null) {
			boolean isCmdFound = false;
			for(IProtocolCmd cmd : cmdList) {
				for(String filter : cmd.getFilters()) {
					if(type.equals(filter)) {
						isCmdFound = true;
						if(!cmd.onCommand(type, msg, helper)) {
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
				helper.getWriter().write(msg);
			}
		}
	}
}