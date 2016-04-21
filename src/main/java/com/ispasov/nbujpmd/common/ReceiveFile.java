package com.ispasov.nbujpmd.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReceiveFile {
	private static final Logger LOG = Logger.getLogger(ReceiveFile.class.getName());

	private long pieces;
	private long currentPiece;
	private String fileName;
	private OutputStream out;
	private String uploadDir;

	private void checkUploadDir() {
		File folder = new File(uploadDir);
		if(!folder.exists() || !folder.isDirectory()) {
			folder.mkdirs();
		}
	}

	public ReceiveFile(String fileName, long pieces) throws IOException, IllegalArgumentException, FileAlreadyExistsException {
		CreateFile(fileName, pieces, "upload/", false);
	}

	public ReceiveFile(String fileName, long pieces, String uploadDir) throws IOException, IllegalArgumentException, FileAlreadyExistsException {
		CreateFile(fileName, pieces, uploadDir, true);
	}

	private void CreateFile(String fileName, long pieces, String uploadDir, boolean del) throws IOException, IllegalArgumentException, FileAlreadyExistsException {
		this.uploadDir = uploadDir;
		checkUploadDir();
		this.fileName = fileName;
		this.pieces = pieces;
		if (fileName == null || pieces < 0 || uploadDir == null) {
			throw new IllegalArgumentException("wrong parameters");
		}
		File saveFile = new File(uploadDir + fileName);
		if (saveFile.exists()) {
			if(del) {
				deleteFile();
			} else {
				throw new FileAlreadyExistsException("file already exist");
			}
		}
		out = new FileOutputStream(saveFile);
		currentPiece = 1;
	}

	public void close() {
		try {
			if(out != null) {
				out.close();
				out = null;
			}
		} catch (IOException ioe) {
			LOG.log(Level.SEVERE, ioe.toString(), ioe);
		}
	}

	public void write(byte[] buffer, long piece) throws IOException, IllegalArgumentException {
		if(piece != currentPiece) {
			throw new IllegalArgumentException("wrong piece number");
		}
		out.write(buffer);
		currentPiece++;
	}

	public boolean isFileReceived() {
		return (currentPiece > pieces);
	}

	public void deleteFile() {
		File saveFile = new File(uploadDir + fileName);
		saveFile.delete();
	}

	public void setPieces(long pieces) {
		this.pieces = pieces;
	}
}