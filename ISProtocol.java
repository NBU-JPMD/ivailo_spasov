import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.lang.IllegalArgumentException;

enum ParseState {
    READ_BEGIN,
	READ_HEADER,
    READ_DATA,
	READ_DONE,
	READ_ERROR
}

class ByteBufferBackedOutputStream extends OutputStream {
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

class ByteBufferBackedInputStream extends InputStream {

    ByteBuffer buf;

    public ByteBufferBackedInputStream(ByteBuffer buf) {
        this.buf = buf;
    }

    public int read() throws IOException {
        if (!buf.hasRemaining()) {
            return -1;
        }
        return buf.get() & 0xFF;
    }

    public int read(byte[] bytes, int off, int len) throws IOException {
        if (!buf.hasRemaining()) {
            return -1;
        }

        len = Math.min(len, buf.remaining());
        buf.get(bytes, off, len);
        return len;
    }
}

public class ISProtocol {
	private ParseState state = ParseState.READ_BEGIN;
	private int buff_size = 1024;
	private int header_size = 6;
	private ByteBuffer buffer_header = ByteBuffer.allocate(header_size);
	private ByteBuffer buffer_data = ByteBuffer.allocate(buff_size);

	private byte data_lrc = 0;
	private int data_size = 0;

	public static byte calculateLRC(byte[] bytes, int length) {
		byte LRC = 0;
		for (int i = 0; i < length; i++) {
			LRC ^= bytes[i];
		}
		return LRC;
	}

	public static byte calculateLRC(byte[] bytes) {
		return calculateLRC(bytes, bytes.length);
	}

	private void read_header(SocketChannel cl) throws IOException {
		if(cl.read(buffer_header) == -1)
			throw new IOException("Connection closed");
		read_header_parse();
	}

	private void read_header_parse() throws IOException {
		if (buffer_header.position() < header_size) {
			state = ParseState.READ_HEADER;
		} else {
			buffer_header.flip();
			data_size = buffer_header.getInt();
			byte header_lrc = calculateLRC(buffer_header.array(), 2);
			if(header_lrc == buffer_header.get() && buff_size >= data_size) {
				state = ParseState.READ_DATA;
				data_lrc = buffer_header.get();
			} else {
				state = ParseState.READ_ERROR;
			}
		}
	}

	private void read_data(SocketChannel cl) throws IOException {
		if(cl.read(buffer_data) == -1)
			throw new IOException("Connection closed");
		read_data_parse();
	}

	private void read_data_parse() throws IOException {
		if (buffer_data.position() < data_size) {
			state = ParseState.READ_DATA;
		} else {
			byte[] data = buffer_data.array();
			byte cl_data_lrc = calculateLRC(data, buffer_data.position());
			if (cl_data_lrc == data_lrc) {
				state = ParseState.READ_DONE;
			} else {
				state = ParseState.READ_ERROR;
			}
		}
	}

	public ParseState read(SocketChannel cl) throws IOException, IllegalArgumentException {
		switch (state) {
			case READ_BEGIN:
			case READ_HEADER:
				read_header(cl);
				break;
			case READ_DATA:
				read_data(cl);
				break;
			default:
				throw new IllegalArgumentException("Wrong state");
		}
		return state;
	}

	private void read_header(InputStream is) throws IOException {
		byte[] data = buffer_header.array();
		int position = buffer_header.position();
		int limit = buffer_header.limit();

		int read = is.read(data, position, limit-position);
		if(read == -1)
			throw new IOException("Connection closed");

		buffer_header.position(read);
		read_header_parse();
	}

	private void read_data(InputStream is) throws IOException {
		byte[] data = buffer_data.array();
		int position = buffer_data.position();
		int limit = buffer_data.limit();

		int read = is.read(data, position, limit-position);
		if(read == -1)
			throw new IOException("Connection closed");

		buffer_data.position(read);
		read_data_parse();
	}

	public ParseState read(InputStream is) throws IOException, IllegalArgumentException {
		switch (state) {
			case READ_BEGIN:
			case READ_HEADER:
				read_header(is);
				break;
			case READ_DATA:
				read_data(is);
				break;
			default:
				throw new IllegalArgumentException("Wrong state");
		}
		return state;
	}

	public ParseState write(SocketChannel cl) throws IOException, IllegalArgumentException {
		if(state == ParseState.READ_DONE) {
			buffer_header.flip();
			buffer_data.flip();

			cl.write(buffer_header);
			cl.write(buffer_data);
		} else {
			throw new IllegalArgumentException("Wrong state");
		}
		return state;
	}

	public ParseState write(OutputStream os) throws IOException, IllegalArgumentException {
		if(state == ParseState.READ_DONE) {
			buffer_header.flip();
			buffer_data.flip();

			os.write(buffer_header.array(), 0, buffer_header.limit());
			os.write(buffer_data.array(), 0, buffer_data.limit());
			os.flush();
		} else {
			throw new IllegalArgumentException("Wrong state");
		}
		return state;
	}

	public ParseState getState() {
		return state;
	}

	public void reset() {
		state = ParseState.READ_BEGIN;
		buffer_header.clear();
		buffer_data.clear();
	}

	public ISMsg getMsg() throws IOException {
		if(state != ParseState.READ_DONE)
			throw new IllegalArgumentException("Wrong state");

		buffer_data.flip();

		try (ByteBufferBackedInputStream bis = new ByteBufferBackedInputStream(buffer_data);
			 ObjectInput in = new ObjectInputStream(bis)) {
			return (ISMsg)in.readObject();
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
			return null;
		}
	}

	public void setMsg(ISMsg pr) throws IOException {
		buffer_data.clear();

		try (ByteBufferBackedOutputStream bos = new ByteBufferBackedOutputStream(buffer_data);
			 ObjectOutput out = new ObjectOutputStream(bos)) {
			out.writeObject(pr);
		}

		byte[] data = buffer_data.array();
		data_size = buffer_data.position();
		data_lrc = calculateLRC(data, data_size);

		buffer_header.clear();
		buffer_header.putInt(data_size);
		buffer_header.put(calculateLRC(buffer_header.array(), 2));
		buffer_header.put(data_lrc);

		state = ParseState.READ_DONE;
	}
}