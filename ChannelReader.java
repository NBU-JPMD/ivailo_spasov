import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class ChannelReader {
	private final static int bufferSize = 1024;
	private final static int headerSize = 4;

	private ByteBuffer clientBuffer = ByteBuffer.allocate(bufferSize);
	private SocketChannel socketChannel = null;
	private int sizeIndex = 0;

	public ChannelReader(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}

	public ArrayList<Object> recv() throws IOException {
		if(socketChannel.read(clientBuffer) == -1) {
			throw new IOException("Connection closed");
		}

		ArrayList<Object> ret = new ArrayList<>();
		Object readObject;

		while((readObject = readObject()) != null) {
			ret.add(readObject);
		}

		if(ret.isEmpty()) {
			return null;
		}

		return ret;
	}

	private int getObjectSize() throws IOException {
		int size = 0;
		int currentSize = clientBuffer.position();
		if(currentSize >= headerSize+sizeIndex) {
			size = clientBuffer.getInt(sizeIndex);
			if(size > bufferSize - headerSize || size <= 0) {
				throw new IOException("wrong recv message size");
			}
		}

		return size;
	}

	private void compactBuffer() {
		if (sizeIndex > 0) {
			byte[] array = clientBuffer.array();
			System.arraycopy(array, sizeIndex, array, 0, clientBuffer.position()-sizeIndex);
			clientBuffer.position(clientBuffer.position()-sizeIndex);
			sizeIndex = 0;
		}
	}

	private Object getObject(int objSize) throws IOException {
		int currentSize = clientBuffer.position();
		if(currentSize - sizeIndex >= objSize + headerSize) {
			clientBuffer.position(sizeIndex+headerSize);
			try (ByteBufferBackedInputStream bis = new ByteBufferBackedInputStream(clientBuffer);
				 ObjectInput in = new ObjectInputStream(bis)) {
				Object ret = in.readObject();
				if(clientBuffer.position() == currentSize) {
					clientBuffer.clear();
					sizeIndex = 0;
				} else {
					sizeIndex = clientBuffer.position();
					clientBuffer.position(currentSize);
				}
				return ret;
			} catch (Exception e) {
				e.printStackTrace();
				clientBuffer.clear();
				throw new IOException("unable to read object from buff");
			}
		}
		return null;
	}

	private Object readObject() throws IOException {
		int objSize = getObjectSize();

		if(objSize != 0) {
			Object ret = getObject(objSize);
			if(ret == null) {
				compactBuffer();
			}
			return ret;
		}

		return null;
	}
}