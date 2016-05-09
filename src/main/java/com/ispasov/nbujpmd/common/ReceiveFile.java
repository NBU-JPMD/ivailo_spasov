package com.ispasov.nbujpmd.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReceiveFile implements AutoCloseable {
	private static final Logger LOG = Logger.getLogger(ReceiveFile.class.getName());
	private static final long MAXFILESIZE = 1 * 1024 * 1024; //1 MB
	private long pieces;
	private long piceSize;
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

	public ReceiveFile(String fileName, String uploadDir, boolean deleteOnExist) throws IOException, IllegalArgumentException, FileAlreadyExistsException {
		if(fileName == null) {
			throw new IllegalArgumentException("file name is null");
		}

		if(fileName == null) {
			throw new IllegalArgumentException("upload direcory is null");
		}

		this.uploadDir = uploadDir;
		this.fileName = fileName;
		checkUploadDir();

		File saveFile = new File(uploadDir + fileName);
		if (saveFile.exists()) {
			if(deleteOnExist) {
				deleteFile();
			} else {
				throw new FileAlreadyExistsException("file already exist");
			}
		}
	}

	public void setParams(long pieces, long fileSize, long piceSize) throws IOException, IllegalArgumentException {
		close();

		if (fileSize < 0 || fileSize > MAXFILESIZE) {
			throw new IllegalArgumentException("file is too big");
		}

		if(pieces < 0 ) {
			throw new IllegalArgumentException("wrong pieces size");
		}

		if(piceSize < 0 || piceSize*(pieces-1) > fileSize) {
			throw new IllegalArgumentException("wrong piece size");
		}

		currentPiece = 1;
		this.pieces = pieces;
		this.piceSize = piceSize;

		out = new FileOutputStream(new File(uploadDir + fileName));
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

		if(buffer.length > piceSize) {
			throw new IllegalArgumentException("pice size is too big");
		}

		if(out == null) {
			throw new IllegalArgumentException("file is not open");
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

	public String getFileName() {
		return fileName;
	}
}