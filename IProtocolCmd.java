import java.io.IOException;

public interface IProtocolCmd {
	public boolean onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException;
	public String[] getFilters();
}