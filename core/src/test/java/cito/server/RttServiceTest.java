package cito.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.websocket.PongMessage;
import javax.websocket.Session;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.slf4j.Logger;

/**
 * Unit tests for {@link RttService}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [30 Jan 2018]
 */
public class RttServiceTest {
	@Rule
	public MockitoRule mockito = MockitoJUnit.rule();

	@Mock
	private Logger log;
	@Mock
	private ManagedScheduledExecutorService executor;

	@InjectMocks
	private RttService rttService;

	@Test
	public void start() {
		final Session session = mock(Session.class);

		this.rttService.start(session);

		verify(this.executor).submit(any(Runnable.class));
		verifyNoMoreInteractions(session);
	}

	@Test
	public void pong() {
		final Session session = mock(Session.class);
		final PongMessage pongMessage = mock(PongMessage.class);
		when(pongMessage.getApplicationData()).thenReturn(
				(ByteBuffer) ByteBuffer.allocate(Long.BYTES).putLong(123).flip());
		when(session.getId()).thenReturn("sessionId");
		when(session.getUserProperties()).thenReturn(new HashMap<>());

		this.rttService.pong(session, pongMessage);

		verify(pongMessage).getApplicationData();
		verify(session).getId();
		verify(this.log).info(eq("RTT {}ms. [id={}]"), any(), eq("sessionId"));
		verify(session).getUserProperties();
		verify(this.executor).schedule(
				any(Runnable.class),
				eq(600_000L),
				eq(TimeUnit.MILLISECONDS));
		verifyNoMoreInteractions(session, pongMessage);
	}

	@After
	public void after() {
		verifyNoMoreInteractions(this.log, this.executor);
	}
}
