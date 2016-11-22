package cito.stomp;

import static cito.stomp.Command.ABORT;
import static cito.stomp.Command.ACK;
import static cito.stomp.Command.BEGIN;
import static cito.stomp.Command.COMMIT;
import static cito.stomp.Command.CONNECT;
import static cito.stomp.Command.CONNECTED;
import static cito.stomp.Command.DISCONNECT;
import static cito.stomp.Command.ERROR;
import static cito.stomp.Command.MESSAGE;
import static cito.stomp.Command.NACK;
import static cito.stomp.Command.RECIEPT;
import static cito.stomp.Command.SEND;
import static cito.stomp.Command.STOMP;
import static cito.stomp.Command.SUBSCRIBE;
import static cito.stomp.Command.UNSUBSCRIBE;
import static cito.stomp.Frame.NULL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.core.MediaType;

import org.junit.Test;

import cito.stomp.Command;
import cito.stomp.Frame;

/**
 * Defines a STOMP frame
 * 
 * @author Daniel Siviter
 * @since v1.0 [12 Jul 2016]
 */
public class FrameTest {
	@Test
	public void from_reader() throws IOException {
		final String input = "MESSAGE\nheader2:value\nheader1:value2\nheader1:value1\n\nbody" + NULL;
		final Frame frame;
		try (StringReader reader = new StringReader(input))  {
			frame = Frame.from(reader);
		}
		assertEquals(Command.MESSAGE, frame.getCommand());
		assertEquals(2, frame.getHeaders().size());

		// ensure header order is maintained
		final Iterator<Entry<String, List<String>>> itr = frame.getHeaders().entrySet().iterator();
		final Entry<String, List<String>> header2 = itr.next();
		assertEquals("header2", header2.getKey());
		assertEquals(1, header2.getValue().size());
		assertEquals("value", header2.getValue().get(0));
		final Entry<String, List<String>> header1 = itr.next();
		assertEquals("header1", header1.getKey());
		assertEquals(2, header1.getValue().size());
		assertEquals("value2", header1.getValue().get(0));
		assertEquals("value1", header1.getValue().get(1));
		assertEquals(ByteBuffer.wrap("body".getBytes(StandardCharsets.UTF_8)), frame.getBody());
	}

	@Test
	public void from_string() {
		Frame frame = Frame.from("ACK\nheader1:value\n\n" + NULL);
		assertEquals(Command.ACK, frame.getCommand());
		assertEquals(1, frame.getHeaders().size());
		final List<String> header1 = frame.getHeaders("header1");
		assertEquals(1, header1.size());
		assertEquals("value", header1.get(0));
		assertNull(frame.getBody());
	}

	@Test
	public void toString_() {
		final Frame frame0 = Frame.message("/here", "sub-0", "123", MediaType.TEXT_PLAIN_TYPE, "body").build();
		assertEquals("MESSAGE\ndestination:/here\nmessage-id:123\ncontent-type:text/plain\nsubscription:sub-0\n\nbody" + NULL, frame0.toString());

		final Frame frame1 = Frame.send("/there", MediaType.TEXT_PLAIN_TYPE, "body").build();
		assertEquals("SEND\ndestination:/there\ncontent-type:text/plain\n\nbody" + NULL, frame1.toString());
	}

	@Test
	public void builder_body() {
		final Command[] commands = {
				ABORT,
				ACK,
				BEGIN,
				COMMIT,
				CONNECT,
				CONNECTED,
				DISCONNECT,
				NACK,
				RECIEPT,
				STOMP,
				SUBSCRIBE,
				UNSUBSCRIBE
		};

		for (Command command : commands) {
			try {
				Frame.builder(command).body(MediaType.TEXT_PLAIN_TYPE, "");
				fail("IllegalArgumentException expected!");
			} catch (RuntimeException e) {
				assertEquals(IllegalArgumentException.class, e.getClass());
				assertEquals(command + " does not accept a body!", e.getMessage());
			}
		}

		for (Command command : new Command[] { SEND, MESSAGE, ERROR }) {
			assertNotNull(Frame.builder(command).body(MediaType.TEXT_PLAIN_TYPE, "blagh"));
		}
	}
}
