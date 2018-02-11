package cito.server;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.websocket.Session;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Unit test for {@link SecurityContextProducer}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [31 Dec 2017]
 */
public class SecurityContextProducerTest {
	@Rule
	public MockitoRule mockito = MockitoJUnit.rule();

	@Mock
	private Session session;
	@Mock
	private SecurityContext securityCtx;

	@Test
	public void securityCtx() {
		when(this.session.getUserProperties()).thenReturn(singletonMap(SecurityContext.class.getName(), this.securityCtx));

		final SecurityContext actual = SecurityContextProducer.securityCtx(this.session);

		assertEquals(this.securityCtx, actual);

		verify(this.session).getUserProperties();
	}

	@Test
	public void securityCtx_noop() {
		when(this.session.getUserProperties()).thenReturn(emptyMap());

		final SecurityContext actual = SecurityContextProducer.securityCtx(this.session);

		assertEquals(SecurityContext.NOOP, actual);

		verify(this.session).getUserProperties();
	}

	@Test
	public void set() {
		final Map<String, Object> properties = new HashMap<>();
		when(this.session.getUserProperties()).thenReturn(properties);

		SecurityContextProducer.set(this.session, this.securityCtx);

		assertEquals(this.securityCtx, properties.get(SecurityContext.class.getName()));

		verify(this.session).getId();
		verify(this.session, times(2)).getUserProperties();
	}

	@After
	public void after() {
		verifyNoMoreInteractions(this.session, this.securityCtx);
	}
}
