package cito.server.ws;

import static cito.stomp.Frame.NULL;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.websocket.DecodeException;
import javax.websocket.EncodeException;

import org.junit.Before;
import org.junit.Test;

import cito.server.ws.FrameEncoding;
import cito.stomp.Frame;

/**
 * Unit test for {@link FrameEncoding}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Jul 2016]
 */
public class FrameEncodingTest {
	private FrameEncoding frameEncoding;

	@Before
	public void before() {
		this.frameEncoding = new FrameEncoding();
	}

	@Test
	public void encode() throws EncodeException, IOException {
		final Frame frame = Frame.receipt("123").build();
		final StringWriter writer = new StringWriter();
		this.frameEncoding.encode(frame, writer);
		assertEquals("RECIEPT\nreceipt-id:123\n\n" + NULL, writer.toString());
	}

	@Test
	public void decode() throws DecodeException, IOException {
		final String input = "MESSAGE\nheader2:value\nheader1:value2\nheader1:value1\n\nbody" + NULL;
		final StringReader reader = new StringReader(input);
		final Frame frame = this.frameEncoding.decode(reader);
		assertEquals(input, frame.toString());
	}
}
