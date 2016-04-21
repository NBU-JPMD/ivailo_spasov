package com.ispasov.nbujpmd.common.channel;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.ArrayList;

public class ChannelReader {
	private final static int BUFFERSIZE = 8 * 1024;
	private final static int HEADERSIZE = 4;

	private final ByteBuffer clientBuffer = ByteBuffer.allocate(BUFFERSIZE);
	private SocketChannel socketChannel = null;
	private int sizeIndex = 0;

	public ChannelReader(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}

	public synchronized List<Object> recv() throws IOException {
		if(socketChannel.read(clientBuffer) == -1) {
			throw new IOException("Connection closed");
		}

		List<Object> ret = new ArrayList<>();
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
		if(currentSize >= HEADERSIZE + sizeIndex) {
			size = clientBuffer.getInt(sizeIndex);
			if(size > BUFFERSIZE - HEADERSIZE || size <= 0) {
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
		if(currentSize - sizeIndex >= objSize + HEADERSIZE) {
			clientBuffer.position(sizeIndex + HEADERSIZE);
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