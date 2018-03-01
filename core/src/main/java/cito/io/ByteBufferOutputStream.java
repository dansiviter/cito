package cito.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import javax.annotation.Nonnull;

/**
 * Simple wrapper to use a {@link ByteBuffer} as an {@link OutputStream}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [24 Feb 2018]
 */
public class ByteBufferOutputStream extends OutputStream {
	private final ByteBuffer buf;

	public ByteBufferOutputStream(@Nonnull ByteBuffer buf) {
		this.buf = buf;
	}

	@Override
	public void write(int b) throws IOException {
		this.buf.put((byte)b);
	}
}
