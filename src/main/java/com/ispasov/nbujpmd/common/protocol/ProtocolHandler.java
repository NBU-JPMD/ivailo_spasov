package com.ispasov.nbujpmd.common.protocol;

import com.ispasov.nbujpmd.common.channel.ChannelHelper;
import com.ispasov.nbujpmd.common.protocol.cmd.ErrorCmd;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public final class ProtocolHandler {
	private final List<IProtocolCmd> cmdList = new ArrayList<>();
	private final ExecutorService fixedPool;

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
				helper.getWriter().write(msg);

			}
		} catch (IOException ioe) {
			LOG.log(Level.SEVERE, ioe.toString(), ioe);
		}
	}
}