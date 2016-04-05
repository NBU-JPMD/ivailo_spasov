import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.lang.IllegalArgumentException;

public class ReceiveFile {
	public static final String uploadDir  = "upload/";

	private long pieces;
	private long currentPiece;
	private String fileName;
	private OutputStream out;

	private static void checkUploadDir() {
		File folder = new File(uploadDir);
		if(!folder.exists() || !folder.isDirectory()) {
			folder.mkdirs();
		}
	}

	public ReceiveFile(String fileName, long pieces) throws IOException, IllegalArgumentException, FileAlreadyExistsException {
		checkUploadDir();
		this.fileName = fileName;
		this.pieces = pieces;
		if (fileName == null || pieces < 0) {
			throw new IllegalArgumentException("wrong parameters");
		}
		File saveFile = new File(uploadDir + fileName);
		if (saveFile.exists()) {
			throw new FileAlreadyExistsException("file already exist");
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
			ioe.printStackTrace();
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
}