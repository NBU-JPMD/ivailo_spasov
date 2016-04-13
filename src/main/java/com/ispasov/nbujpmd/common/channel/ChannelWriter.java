package com.ispasov.nbujpmd.common.channel;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class ChannelWriter {
	private final static int bufferSize = 8 * 1024;
	private final static int headerSize = 4;

	private ByteBuffer clientBuffer = ByteBuffer.allocate(bufferSize);
	private SocketChannel socketChannel = null;

	public ChannelWriter (SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}

	public synchronized void write(Object obj) throws IOException {
		clientBuffer.clear();
		clientBuffer.position(headerSize);

		try (ByteBufferBackedOutputStream bos = new ByteBufferBackedOutputStream(clientBuffer);
			 ObjectOutput out = new ObjectOutputStream(bos)) {
			out.writeObject(obj);
		}
		clientBuffer.putInt(0, clientBuffer.position()-headerSize);
		clientBuffer.flip();
		socketChannel.write(clientBuffer);
	}

	public synchronized void write(ArrayList<Object> arrayList) throws IOException {
		for(Object obj : arrayList) {
			write(obj);
		}
	}
}