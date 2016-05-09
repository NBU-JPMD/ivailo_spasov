package com.ispasov.nbujpmd.common.protocol;

import com.ispasov.nbujpmd.common.channel.ChannelHelper;
import com.ispasov.nbujpmd.common.protocol.cmd.ErrorCmd;

import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public final class ExecutorProtocolHandler implements IProtocolHandler {
	private final List<IProtocolCmd> cmdList = new ArrayList<>();
	private final ExecutorService fixedPool;

	public ExecutorProtocolHandler(int nThreads) {
		fixedPool = Executors.newFixedThreadPool(nThreads);
		registerCommand(new ErrorCmd());
	}

	@Override
	public void registerCommand(IProtocolCmd cmd) {
		cmdList.add(cmd);
	}

	@Override
	public void handleMsg(String type, ISMsg msg, ChannelHelper helper, Object data) {
		if(type != null) {
			Runnable worker = new ProtocolWorkerThread(cmdList, type, msg, helper, data);
			fixedPool.execute(worker);
		}
	}

	@Override
	public void shutdown() {
		fixedPool.shutdown();
	}
}

class ProtocolWorkerThread implements Runnable {
	private static final Logger LOG = Logger.getLogger(ProtocolWorkerThread.class.getName());

	private final List<IProtocolCmd> cmdList;
	private final String type;
	private ISMsg msg;
	private final ChannelHelper helper;
	private final Object data;

	public ProtocolWorkerThread(List<IProtocolCmd> cmdList, String type, ISMsg msg, ChannelHelper helper, Object data) {
		this.cmdList = cmdList;
		this.type = type;
		this.msg = msg;
		this.helper = helper;
		this.data = data;
	}

	@Override
	public void run() {
		try {
			List<IProtocolCmd> runCmd = cmdList.stream()
				.filter(cmd -> {
					return Arrays.stream(cmd.getFilters())
						.filter(f -> type.equals(f))
						.findAny()
						.isPresent();
				})
				.collect(Collectors.toList());

			if(!runCmd.isEmpty()) {
				for(IProtocolCmd cmd : runCmd) {
					cmd.onCommand(type, msg, helper, data);
				}
			} else {
				msg = new ISMsg();
				msg.setRespCode(101);
				msg.addKey("type", "error");
				msg.addKey("msg", "Unsupported message type.");
				helper.getWriter().write(msg);
			}
		} catch (IOException ioe) {
			LOG.log(Level.SEVERE, ioe.toString(), ioe);
		}
	}
}