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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.enterprise.inject.Instance;
import javax.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for {@link Serialiser}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [16 Apr 2017]
 */
@RunWith(MockitoJUnitRunner.class)
public class SerialiserTest {
	@Mock
	private Instance<BodyReader<?>> readers;
	@Mock
	private Instance<BodyWriter<?>> writers;

	@InjectMocks
	private Serialiser serialiser;

	@Test
	public void readFrom() throws IOException {
		final InputStream is = mock(InputStream.class);
		final MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;
		@SuppressWarnings("unchecked")
		final BodyReader<SerialiserTest> reader = mock(BodyReader.class);
		@SuppressWarnings("rawtypes")
		final Iterator iterator = asList(reader).iterator();
		when(readers.iterator()).thenReturn(iterator);
		when(reader.isReadable(SerialiserTest.class, mediaType)).thenReturn(true);
		when(reader.readFrom(SerialiserTest.class, mediaType, is)).thenReturn(this);

		final SerialiserTest obj = this.serialiser.readFrom(SerialiserTest.class, mediaType, is);
		assertEquals(this, obj);

		verify(readers).iterator();
		verify(reader).isReadable(SerialiserTest.class, mediaType);
		verify(reader).readFrom(SerialiserTest.class, mediaType, is);
		verifyNoMoreInteractions(is, reader);
	}

	@Test
	public void writeTo() throws IOException {
		final OutputStream os = Mockito.mock(OutputStream.class);
		final MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;
		@SuppressWarnings("unchecked")
		final BodyWriter<SerialiserTest> writer = mock(BodyWriter.class);
		@SuppressWarnings("rawtypes")
		final Iterator iterator = asList(writer).iterator();
		when(writers.iterator()).thenReturn(iterator);
		when(writer.isWriteable(SerialiserTest.class, mediaType)).thenReturn(true);

		this.serialiser.writeTo(this, SerialiserTest.class, mediaType, os);

		verify(writers).iterator();
		verify(writer).isWriteable(SerialiserTest.class, mediaType);
		verify(writer).writeTo(this, SerialiserTest.class, mediaType, os);
		verifyNoMoreInteractions(os);
	}

	@After
	public void after() {
		verifyNoMoreInteractions(this.readers, this.writers);
	}
}
