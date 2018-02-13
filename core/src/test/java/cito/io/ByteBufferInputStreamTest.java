package cito.io;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;

/**
 * Unit test for {@link ByteBufferInputStream}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [11 Feb 2018]
 */
public class ByteBufferInputStreamTest {
	@Test
	public void read() throws IOException {
		final byte[] bytes = "input".getBytes();
		try (ByteBufferInputStream is = new ByteBufferInputStream(ByteBuffer.wrap(bytes))) {
			for (int i = 0; i< bytes.length; i++) {
				assertEquals(bytes[i], is.read());
			}
			assertEquals(-1, is.read());
		}
	}
}
