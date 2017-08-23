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
package cito.stomp.jms;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import javax.security.auth.login.LoginException;
import javax.websocket.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import cito.ReflectionUtil;
import cito.event.Message;
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
	private Event<Message> messageEvent;
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
		when(this.securityRegistry.isPermitted(any(Frame.class), eq(this.securityCtx))).thenReturn(true);
	}

	@Test
	public void fromClient_CONNECT() throws JMSException, LoginException {
		final Frame frame = Frame.builder(Command.CONNECT).header(Headers.HOST, "host").header(Headers.ACCEPT_VERSION, "1.1").build();
		final Message msg = new Message("sessionId", frame);
		this.relay.fromClient(msg);

		verify(this.log).debug("Message from client. [sessionId={},command={}]", "sessionId", Command.CONNECT);
		verify(this.securityCtxProvider).get();
		verify(this.securityRegistry).isPermitted(frame, this.securityCtx);
		verify(this.log).info("CONNECT/STOMP recieved. Opening connection to broker. [sessionId={}]", "sessionId");
		verify(this.connectionInstance).get();
		verify(this.connection).connect(msg);
	}

	@Test
	public void fromClient_STOMP() throws JMSException, LoginException {
		final Frame frame = Frame.builder(Command.STOMP).header(Headers.HOST, "host").header(Headers.ACCEPT_VERSION, "1.1").build();
		final Message msg = new Message("sessionId", frame);
		this.relay.fromClient(msg);

		verify(this.log).debug("Message from client. [sessionId={},command={}]", "sessionId", Command.STOMP);
		verify(this.securityCtxProvider).get();
		verify(this.securityRegistry).isPermitted(frame, this.securityCtx);
		verify(this.log).info("CONNECT/STOMP recieved. Opening connection to broker. [sessionId={}]", "sessionId");
		verify(this.connectionInstance).get();
		verify(this.connection).connect(msg);
	}

	@Test
	public void fromClient_DISCONNECT() throws IOException {
		final Session session = mock(Session.class);

		ReflectionUtil.<Map<String, Connection>>get(this.relay, "connections").put("sessionId", this.connection);
		when(this.sessionRegistry.getSession("sessionId")).thenReturn(Optional.of(session));
		when(session.isOpen()).thenReturn(true);

		final Frame frame = Frame.disconnect().build();
		final Message msg = new Message("sessionId", frame);
		this.relay.fromClient(msg);

		verify(this.log).debug("Message from client. [sessionId={},command={}]", "sessionId", Command.DISCONNECT);
		verify(this.securityCtxProvider).get();
		verify(this.securityRegistry).isPermitted(frame, this.securityCtx);
		verify(this.log).info("DISCONNECT recieved. Closing connection to broker. [sessionId={}]", "sessionId");
		verify(this.log).info("Destroying JMS connection. [{}]", "sessionId");
		verify(this.connectionInstance).destroy(this.connection);
		verify(this.sessionRegistry).getSession("sessionId");
		verify(this.connection).disconnect(any(Message.class));
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
