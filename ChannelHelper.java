import java.nio.channels.SocketChannel;

public class ChannelHelper {
	private ChannelReader channelReader = null;
	private ChannelWriter channelWriter = null;

	public ChannelHelper(SocketChannel client) {
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