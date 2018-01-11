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
package cito.stomp.ws;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.websocket.DecodeException;

import org.junit.Before;
import org.junit.Test;

import cito.stomp.Command;
import cito.stomp.Frame;

/**
 * Unit test for {@link FrameDecoder}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [24 Nov 2017]
 */
public class FrameDecoderTest {
	private FrameDecoder.Binary binary;
	private FrameDecoder.Text text;

	@Before
	public void before() {
		this.binary = new FrameDecoder.Binary();
		this.text = new FrameDecoder.Text();
	}

	@Test
	public void decode_byteBuffer() throws DecodeException, IOException {
		final String input = "MESSAGE\ndestination:wonderland\nsubscription:a\ncontent-length:4\n\nbody\u0000";
		final Frame frame;
		try (InputStream is = new ByteArrayInputStream(input.getBytes()))  {
			frame = binary.decode(ByteBuffer.wrap(input.getBytes(UTF_8)));
		}
		assertEquals(Command.MESSAGE, frame.command());
	}

	@Test
	public void decode_string() throws DecodeException {
		final String input = "MESSAGE\ndestination:wonderland\nsubscription:a\ncontent-length:4\n\nbody\u0000";
		final Frame frame = text.decode(input);
		assertEquals(Command.MESSAGE, frame.command());
	}
}
