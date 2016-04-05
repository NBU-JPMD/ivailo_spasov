import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.lang.Math;

public class SendFile {
	public static final long pieceSize = 2 * 1024;

	private long pieces;
	private long currentPiece;
	private InputStream in;
	private String fileName;
	private long length;

	public SendFile(String fileName) throws IOException {
		File inFile = new File(fileName);
		if(!inFile.exists() || inFile.isDirectory()) { 
			throw new IOException("missing file");
		}

		currentPiece = 1;
		length = inFile.length();
		pieces = (length + pieceSize - 1)/pieceSize;

		in = new FileInputStream(fileName);
		this.fileName = inFile.getName();
	}

	public ISMsg getUploadMsg() {
		ISMsg msg = new ISMsg();
		msg.addKey("type", "upload");
		msg.addKey("file", fileName);
		msg.addKey("pieces", pieces);
		return msg;
	}

	public ISMsg getNextMsg() throws IOException {
		long remBytes = length - ((currentPiece - 1)*pieceSize);
		int buffSize = (int)Math.min(pieceSize, remBytes);

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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isFileSend() {
		return (currentPiece > pieces);
	}
}