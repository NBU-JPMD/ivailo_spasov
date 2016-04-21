package com.ispasov.nbujpmd.common.channel;

import java.nio.ByteBuffer;
import java.io.IOException;
import java.io.InputStream;

class ByteBufferBackedInputStream extends InputStream {
    ByteBuffer buf;

    public ByteBufferBackedInputStream(ByteBuffer buf) {
        this.buf = buf;
    }

    @Override
    public int read() throws IOException {
        if (!buf.hasRemaining()) {
            return -1;
        }
        return buf.get() & 0xFF;
    }

    @Override
    public int read(byte[] bytes, int off, int len) throws IOException {
        if (!buf.hasRemaining()) {
            return -1;
        }

        len = Math.min(len, buf.remaining());
        buf.get(bytes, off, len);
        return len;
    }
}