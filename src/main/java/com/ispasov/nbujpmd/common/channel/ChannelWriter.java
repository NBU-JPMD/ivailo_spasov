package com.ispasov.nbujpmd.common.channel;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.List;

public class ChannelWriter {
	private final static int BUFFERSIZE = 8 * 1024;
	private final static int HEADERSIZE = 4;

	private final ByteBuffer clientBuffer = ByteBuffer.allocate(BUFFERSIZE);
	private SocketChannel socketChannel = null;

	public ChannelWriter (SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}

	public synchronized void write(Object obj) throws IOException {
		clientBuffer.clear();
		clientBuffer.position(HEADERSIZE);

		try (ByteBufferBackedOutputStream bos = new ByteBufferBackedOutputStream(clientBuffer);
			 ObjectOutput out = new ObjectOutputStream(bos)) {
			out.writeObject(obj);
		}
		clientBuffer.putInt(0, clientBuffer.position()-HEADERSIZE);
		clientBuffer.flip();
		socketChannel.write(clientBuffer);
	}

	public synchronized void write(List<Object> arrayList) throws IOException {
		for (Object obj : arrayList) {
			write(obj);
		}
	}
}