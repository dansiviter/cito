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

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.websocket.EncodeException;

import org.junit.Before;
import org.junit.Test;

import cito.stomp.Frame;

/**
 * Unit test for {@link FrameEncoder}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Jul 2016]
 */
public class FrameEncoderTest {
	private FrameEncoder.Binary binary;
	private FrameEncoder.Text text;

	@Before
	public void before() {
		this.binary = new FrameEncoder.Binary();
		this.text = new FrameEncoder.Text();
	}

	@Test
	public void encode_byteBuffer() throws EncodeException, IOException {
		final Frame frame = Frame.receipt("123").build();
		final ByteBuffer actual = this.binary.encode(frame);
		assertEquals(UTF_8.encode("RECEIPT\nreceipt-id:123\n\n\u0000"), actual);
	}

	@Test
	public void encode_string() throws EncodeException, IOException {
		final Frame frame = Frame.receipt("123").build();
		final String actual = this.text.encode(frame);
		assertEquals("RECEIPT\nreceipt-id:123\n\n\u0000", actual);
	}
}
