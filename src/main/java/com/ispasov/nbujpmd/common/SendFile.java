package com.ispasov.nbujpmd.common;

import com.ispasov.nbujpmd.common.protocol.ISMsg;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SendFile {
	private static final Logger LOG = Logger.getLogger(SendFile.class.getName());
	private static final long PIECESIZE = 2 * 1024;

	private long pieces;
	private long currentPiece;
	private InputStream in;
	private String fileName;
	private long length;
	private String uploadType;

	public SendFile(String fileName) throws IOException {
		CreateFile(fileName, "upload");
	}

	public SendFile(String fileName, String uploadType) throws IOException {
		CreateFile(fileName, uploadType);
	}

	private void CreateFile(String fileName, String uploadType) throws IOException {
		File inFile = new File(fileName);
		if(!inFile.exists() || inFile.isDirectory()) {
			throw new IOException("missing file");
		}

		currentPiece = 1;
		length = inFile.length();
		pieces = (length + PIECESIZE - 1) / PIECESIZE;

		in = new FileInputStream(fileName);
		this.fileName = inFile.getName();
		this.uploadType = uploadType;
	}

	public ISMsg getUploadMsg() {
		ISMsg msg = new ISMsg();
		msg.addKey("type", uploadType);
		msg.addKey("file", fileName);
		msg.addKey("pieces", pieces);
		return msg;
	}

	public ISMsg getNextMsg() throws IOException {
		long remBytes = length - ((currentPiece - 1) * PIECESIZE);
		int buffSize = (int)Math.min(PIECESIZE, remBytes);

		byte[] dataBuff = new byte[buffSize];

		if (in.read(dataBuff) != buffSize) {
			throw new IOException("read file error");
		}

		ISMsg msg = new ISMsg();
		msg.addKey("type", "piece");
		msg.addKey("data", dataBuff);
		msg.addKey("piece", currentPiece);
		currentPiece++;
		return msg;
	}

	public void close() {
		try {
			if(in != null) {
				in.close();
				in = null;
			}
		} catch (IOException ioe) {
			LOG.log(Level.SEVERE, ioe.toString(), ioe);
		}
	}

	public boolean isFileSend() {
		return (currentPiece > pieces);
	}
}