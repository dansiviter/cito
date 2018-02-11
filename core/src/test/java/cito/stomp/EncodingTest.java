/*

 * Copyright 2016-2017 Daniel Siviter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cito.stomp;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.websocket.DecodeException;
import javax.websocket.EncodeException;

import org.junit.Test;

/**
 * Unit tests for {@link Encoding}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Nov 2017]
 */
public class EncodingTest {
	@Test
	public void isFrame_heartbeat() {
		assertTrue(Encoding.isFrame(ByteBuffer.wrap(Frame.HEART_BEAT.toString().getBytes())));
	}

	@Test
	public void isFrame_message() {
		assertTrue(Encoding.isFrame(Encoding.from(Frame.message("destination", "subscriptionId", "messageId", null, "message").build(), false, 1024)));
	}

	@Test
	public void from_byteBuffer() throws IOException, DecodeException {
		final String input = "MESSAGE\ndestination:wonderland\nsubscription:a\ncontent-length:4\n\nbody\u0000";
		final Frame frame;
		try (InputStream is = new ByteArrayInputStream(input.getBytes()))  {
			frame = Encoding.from(ByteBuffer.wrap(input.getBytes(UTF_8)));
		}
		assertEquals(Command.MESSAGE, frame.command());
		assertEquals(4, frame.headers().size());

		// ensure header order is maintained
		final Iterator<Entry<Header, List<String>>> itr = frame.headers().entrySet().iterator();
		final Entry<Header, List<String>> header2 = itr.next();
		assertEquals("destination", header2.getKey().value());
		assertEquals(1, header2.getValue().size());
		assertEquals("wonderland", header2.getValue().get(0));
		final Entry<Header, List<String>> header1 = itr.next();
		assertEquals("subscription", header1.getKey().value());
		assertEquals(1, header1.getValue().size());
		assertEquals("a", header1.getValue().get(0));
		assertEquals(ByteBuffer.wrap("body".getBytes(UTF_8)), frame.body().get());
	}

	@Test
	public void from_byteBuffer_leadingEoL() throws IOException, DecodeException {
		final String input = "\nMESSAGE\ndestination:wonderland\nsubscription:a\ncontent-length:4\n\nbody\u0000";
		final Frame frame;
		try (InputStream is = new ByteArrayInputStream(input.getBytes()))  {
			frame = Encoding.from(ByteBuffer.wrap(input.getBytes(UTF_8)));
		}
		assertEquals(Command.MESSAGE, frame.command());
		assertEquals(4, frame.headers().size());
	}

	@Test
	public void Encoding_windowsEoL() throws IOException, DecodeException {
		final String input = "MESSAGE\r\ndestination:wonderland\r\nsubscription:a\r\ncontent-length:4\r\n\r\nbody\u0000";
		final Frame frame;
		try (InputStream is = new ByteArrayInputStream(input.getBytes()))  {
			frame = Encoding.from(ByteBuffer.wrap(input.getBytes(UTF_8)));
		}
		assertEquals(Command.MESSAGE, frame.command());
		assertEquals(4, frame.headers().size());
	}

	@Test
	public void from_frame() throws EncodeException, IOException {
		final Frame frame = Frame.receipt("123").build();
		final ByteBuffer actual = Encoding.from(frame, false, 1024);
		assertEquals(ByteBuffer.wrap("RECEIPT\nreceipt-id:123\n\n\u0000".getBytes(UTF_8)), actual);
	}
}
