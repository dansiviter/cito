package cito.ext;

import static cito.ReflectionUtil.set;
import static java.nio.charset.StandardCharsets.UTF_16;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Type;

import javax.enterprise.inject.Instance;
import javax.json.bind.Jsonb;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;

/**
 * Unit tests for {@link JsonBSerialiser}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [11 Feb 2018]
 */
public class JsonBSerialiserTest {
	@Rule
	public MockitoRule mockito = MockitoJUnit.rule();

	@Mock
	private Logger log;
	@Mock
	private Instance<Jsonb> instance;

	@InjectMocks
	private JsonBSerialiser serialiser;

	@Test
	public void isReadable() {
		assertTrue(this.serialiser.isReadable(String.class, APPLICATION_JSON_TYPE));
		assertTrue(this.serialiser.isReadable(Reader.class, APPLICATION_JSON_TYPE));

		assertTrue(this.serialiser.isReadable(String.class, APPLICATION_JSON_TYPE.withCharset("UTF-8")));
		assertTrue(this.serialiser.isReadable(Reader.class, APPLICATION_JSON_TYPE.withCharset("UTF-8")));

		assertFalse(this.serialiser.isReadable(String.class, TEXT_PLAIN_TYPE));
	}

	@Test
	public void readFrom() throws IOException {
		when(this.instance.isUnsatisfied()).thenReturn(false);
		final Jsonb jsonb = mock(Jsonb.class);
		when(this.instance.get()).thenReturn(jsonb);
		when(jsonb.fromJson(any(InputStream.class), any(Type.class))).thenReturn("input");

		final String actual = (String) this.serialiser.readFrom(
				String.class, APPLICATION_JSON_TYPE, new ByteArrayInputStream("\"input\"".getBytes(UTF_16)));

		assertEquals("input", actual);

		verify(this.instance).isUnsatisfied();
		verify(this.instance).get();
		verify(jsonb).fromJson(any(InputStream.class), any(Type.class));
		verifyNoMoreInteractions(jsonb);
	}

	@Test
	public void isWriteable() {
		assertTrue(this.serialiser.isWriteable(String.class, APPLICATION_JSON_TYPE));
		assertTrue(this.serialiser.isWriteable(Reader.class, APPLICATION_JSON_TYPE));

		assertTrue(this.serialiser.isWriteable(String.class, APPLICATION_JSON_TYPE.withCharset("UTF-8")));
		assertTrue(this.serialiser.isWriteable(Reader.class, APPLICATION_JSON_TYPE.withCharset("UTF-8")));

		assertFalse(this.serialiser.isWriteable(String.class, TEXT_PLAIN_TYPE));
	}

	@Test
	public void writeTo() throws IOException {
		when(this.instance.isUnsatisfied()).thenReturn(false);
		final Jsonb jsonb = mock(Jsonb.class);
		when(this.instance.get()).thenReturn(jsonb);
		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				((OutputStream) invocation.getArguments()[2]).write("\"input\"".getBytes(UTF_8));
				return null;
			}
		}).when(jsonb).toJson(any(String.class), eq(String.class), any(OutputStream.class));

		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		this.serialiser.writeTo("input", String.class, APPLICATION_JSON_TYPE.withCharset("UTF8"), out);
		assertEquals("\"input\"", new String(out.toByteArray(), UTF_8));

		verify(this.instance).isUnsatisfied();
		verify(this.instance).get();
		verify(jsonb).toJson(any(String.class), eq(String.class), any(OutputStream.class));
		verifyNoMoreInteractions(jsonb);
	}

	@Test
	public void destroy() throws Exception {
		final Jsonb jsonb = mock(Jsonb.class);
		set(this.serialiser, "jsonb", jsonb);

		this.serialiser.destroy();

		verify(jsonb).close();
		verifyNoMoreInteractions(jsonb);
	}

	@Test
	public void destroy_exception() throws Exception {
		final Jsonb jsonb = mock(Jsonb.class);
		set(this.serialiser, "jsonb", jsonb);
		doThrow(new IllegalArgumentException()).when(jsonb).close();

		this.serialiser.destroy();

		verify(jsonb).close();
		verify(this.log).warn(eq("Unable to close Jsonb!"), any(Throwable.class));
		verifyNoMoreInteractions(jsonb);
	}


	@After
	public void after() {
		verifyNoMoreInteractions(this.log, this.instance);
	}
}
