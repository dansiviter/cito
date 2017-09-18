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
		assertEquals("RECEIPT\nreceipt-id:123\n\n" + NULL, writer.toString());
	}

	@Test
	public void decode() throws DecodeException, IOException {
		final String input = "MESSAGE\nheader2:value\nheader1:value2\nheader1:value1\n\nbody" + NULL;
		final StringReader reader = new StringReader(input);
		final Frame frame = this.frameEncoding.decode(reader);
		assertEquals(input, frame.toString());
	}
}
