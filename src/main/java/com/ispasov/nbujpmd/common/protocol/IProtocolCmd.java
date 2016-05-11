package com.ispasov.nbujpmd.common.protocol;

import com.ispasov.nbujpmd.common.channel.ChannelHelper;

import java.io.IOException;
import java.util.Arrays;

public interface IProtocolCmd {
	public void onCommand(String cmd, ISMsg msg, ChannelHelper helper, Object data) throws IOException;
	public String[] getFilters();

	public static boolean matchCommand(String type, IProtocolCmd cmd) {
		return Arrays.stream(cmd.getFilters())
			 .filter(f -> type.equals(f))
			 .findAny()
			 .isPresent();
	}
}