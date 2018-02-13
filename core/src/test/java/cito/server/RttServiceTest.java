package cito.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.websocket.PongMessage;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;
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
	public void start() throws IllegalArgumentException, IOException {
		doAnswer(new Answer<Future<?>>() {
			@Override
			public Future<?> answer(InvocationOnMock invocation) throws Throwable {
				((Runnable) invocation.getArguments()[0]).run();
				return null;
			}
		}).when(executor).submit(any(Runnable.class));
		final Session session = mock(Session.class);
		when(session.isOpen()).thenReturn(true);
		when(session.getId()).thenReturn("sessionId");
		final Basic basic = mock(Basic.class);
		when(session.getBasicRemote()).thenReturn(basic);

		this.rttService.start(session);

		verify(this.executor).submit(any(Runnable.class));
		verify(session).isOpen();
		verify(session).getId();
		verify(this.log).debug("Sending ping. [id={}]", "sessionId");
		verify(session).getBasicRemote();
		verify(basic).sendPing(any());

		verifyNoMoreInteractions(session, basic);
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
