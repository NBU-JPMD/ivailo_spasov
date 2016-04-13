package com.ispasov.nbujpmd.common.channel;

import java.nio.ByteBuffer;
import java.io.IOException;
import java.io.OutputStream;

public class ByteBufferBackedOutputStream extends OutputStream {
    ByteBuffer buf;

    public ByteBufferBackedOutputStream(ByteBuffer buf) {
        this.buf = buf;
    }

    public void write(int b) throws IOException {
        buf.put((byte) b);
    }

    public void write(byte[] bytes, int off, int len) throws IOException {
        buf.put(bytes, off, len);
    }
}