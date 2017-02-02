package cito.stomp.jms;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.inject.Provider;
import javax.jms.JMSException;
import javax.websocket.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import cito.ReflectionUtil;
import cito.event.MessageEvent;
import cito.server.SecurityContext;
import cito.server.SessionRegistry;
import cito.server.security.SecurityRegistry;
import cito.stomp.Command;
import cito.stomp.Frame;
import cito.stomp.Headers;

/**
 * Unit test for {@link Relay}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Jul 2016]
 */
@RunWith(MockitoJUnitRunner.class)
public class RelayTest {
	@Mock
	private Logger log;
	@Mock
	private Event<MessageEvent> messageEvent;
	@Mock
	private SessionRegistry sessionRegistry;
	@Mock
	private ErrorHandler errorHandler;
	@Mock
	private SecurityRegistry securityRegistry;
	@Mock
	private Provider<SecurityContext> securityCtxProvider;
	@Mock
	private SecurityContext securityCtx;
	@Mock
	private Instance<Connection> connectionInstance;
	@Mock
	private Connection connection;
	@Mock
	private SystemConnection systemConn;

	@InjectMocks
	private Relay relay;

	@Before
	public void before() {
		when(this.connectionInstance.get()).thenReturn(this.connection);
		when(this.securityCtxProvider.get()).thenReturn(this.securityCtx);
	}

	@Test
	public void message_CONNECT() throws JMSException {
		final MessageEvent msg = new MessageEvent("sessionId", Frame.builder(Command.CONNECT).header(Headers.HOST, "host").header(Headers.ACCEPT_VERSION, "1.1").build());
		this.relay.on(msg);

		verify(this.log).info("CONNECT/STOMP recieved. Opening connection to broker. [sessionId={}]", "sessionId");
		verify(this.connectionInstance).get();
		verify(this.connection).connect(msg);
	}

	@Test
	public void message_STOMP() throws JMSException {
		final MessageEvent msg = new MessageEvent("sessionId", Frame.builder(Command.STOMP).header(Headers.HOST, "host").header(Headers.ACCEPT_VERSION, "1.1").build());
		this.relay.on(msg);

		verify(this.log).info("CONNECT/STOMP recieved. Opening connection to broker. [sessionId={}]", "sessionId");
		verify(this.connectionInstance).get();
		verify(this.connection).connect(msg);
	}

	@Test
	public void message_DISCONNECT() throws IOException {
		final Session session = mock(Session.class);
		
		ReflectionUtil.<Map<String, Connection>>get(this.relay, "connections").put("sessionId", this.connection);
		when(this.sessionRegistry.getSession("sessionId")).thenReturn(Optional.of(session));
		when(session.isOpen()).thenReturn(true);

		final MessageEvent msg = new MessageEvent("sessionId", Frame.disconnect().build());
		this.relay.on(msg);

		verify(this.log).info("DISCONNECT recieved. Closing connection to broker. [sessionId={}]", "sessionId");
		verify(this.log).info("Destroying JMS connection. [{}]", "sessionId");
		verify(this.connectionInstance).destroy(this.connection);
		verify(this.sessionRegistry).getSession("sessionId");
		verify(this.connection).disconnect(any(MessageEvent.class));
		verify(session).isOpen();
		verify(session).close();
		verifyNoMoreInteractions(session);
	}

	@Test
	public void close_session() throws IOException {
		final Session session = mock(Session.class);
		when(session.getId()).thenReturn("sessionId");
		when(this.sessionRegistry.getSession("sessionId")).thenReturn(Optional.of(session));
		when(session.isOpen()).thenReturn(true);

		this.relay.close(session);

		verify(session).getId();
		verify(this.sessionRegistry).getSession("sessionId");
		verify(session).isOpen();
		verify(session).close();
		verifyNoMoreInteractions(session);
	}

	@Test
	public void send() {
		final MessageEvent msg = mock(MessageEvent.class);
		this.relay.send(msg);

		verifyNoMoreInteractions(msg);
		verify(this.messageEvent).fire(msg);
	}

	@After
	public void after() {
		verifyNoMoreInteractions(
				this.log,
				this.messageEvent,
				this.sessionRegistry,
				this.errorHandler,
				this.securityRegistry,
				this.securityCtxProvider,
				this.securityCtx,
				this.connectionInstance,
				this.connection,
				this.systemConn);
	}
}
