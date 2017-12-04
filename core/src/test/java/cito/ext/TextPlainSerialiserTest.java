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
package cito.ext;

import static java.nio.charset.StandardCharsets.UTF_16;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link TextPlainSerialiser}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [4 Dec 2017]
 */
public class TextPlainSerialiserTest {
	private TextPlainSerialiser serialiser;

	@Before
	public void before() {
		this.serialiser = new TextPlainSerialiser();
	}

	@Test
	public void isReadable() {
		assertTrue(this.serialiser.isReadable(String.class, TEXT_PLAIN_TYPE));
		assertTrue(this.serialiser.isReadable(Reader.class, TEXT_PLAIN_TYPE));

		assertTrue(this.serialiser.isReadable(String.class, TEXT_PLAIN_TYPE.withCharset("UTF-8")));
		assertTrue(this.serialiser.isReadable(Reader.class, TEXT_PLAIN_TYPE.withCharset("UTF-8")));

		assertFalse(this.serialiser.isReadable(String.class, APPLICATION_JSON_TYPE));
	}

	@Test
	public void readFrom() throws IOException {
		final String expected = "input";
		final String actual = (String) this.serialiser.readFrom(
				String.class, TEXT_PLAIN_TYPE.withCharset("UTF-16"), new ByteArrayInputStream(expected.getBytes(UTF_16)));

		assertEquals(expected, actual);
	}

	@Test
	public void isWriteable() {
		assertTrue(this.serialiser.isWriteable(String.class, TEXT_PLAIN_TYPE));
		assertTrue(this.serialiser.isWriteable(Reader.class, TEXT_PLAIN_TYPE));

		assertTrue(this.serialiser.isWriteable(String.class, TEXT_PLAIN_TYPE.withCharset("UTF-8")));
		assertTrue(this.serialiser.isWriteable(Reader.class, TEXT_PLAIN_TYPE.withCharset("UTF-8")));

		assertFalse(this.serialiser.isWriteable(String.class, APPLICATION_JSON_TYPE));
	}

	@Test
	public void writeTo() throws IOException {
		final String expected = "input";
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		this.serialiser.writeTo("input", String.class, TEXT_PLAIN_TYPE.withCharset("UTF8"), out);

		assertEquals(expected, new String(out.toByteArray(), UTF_8));
	}
}
