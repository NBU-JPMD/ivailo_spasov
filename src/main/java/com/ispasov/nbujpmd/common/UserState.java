package com.ispasov.nbujpmd.common;

import com.ispasov.nbujpmd.common.channel.ChannelHelper;

public class UserState implements AutoCloseable {
	private String user = null;
	private ChannelHelper helper = null;
	private ReceiveFile receiveFile = null;
	private SendFile sendFile = null;

	public UserState(ChannelHelper helper) {
		this.helper = helper;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public ChannelHelper getChannelHelper() {
		return helper;
	}

	public void setReceiveFile(ReceiveFile receiveFile) {
		this.receiveFile = receiveFile;
	}

	public ReceiveFile getReceiveFile() {
		return receiveFile;
	}

	public void setSendFile(SendFile sendFile) {
		this.sendFile = sendFile;
	}

	public SendFile getSendFile() {
		return sendFile;
	}

	public void close() {
		if(receiveFile != null) {
			receiveFile.close();
			receiveFile.deleteFile();
			receiveFile = null;
		}
		if(sendFile != null) {
			sendFile.close();
			sendFile = null;
		}
	}
}