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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Provider;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.security.auth.login.LoginException;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import cito.ReflectionUtil;
import cito.event.Message;
import cito.server.SecurityContext;
import cito.stomp.Command;
import cito.stomp.Frame;
import cito.stomp.Header.Standard;
import cito.stomp.HeartBeatMonitor;

/**
 * Unit tests for {@link Connection}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Jul 2016]
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectionTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Mock
	private Logger log;
	@Mock
	private BeanManager beanManager;
	@Mock
	private Relay relay;
	@Mock
	private ConnectionFactory connectionFactory;
	@Mock
	private Factory factory;
	@Mock
	private ScheduledExecutorService scheduler;
	@Mock
	private Event<Message> brokerMessageEvent;
	@Mock
	private Provider<javax.websocket.Session> wsSessionProvider;
	@Mock
	private javax.websocket.Session wsSession;
	@Mock
	private Instance<SecurityContext> securityCtx;

	@InjectMocks
	private Connection connection;

	@Before
	public void before() {
		ReflectionUtil.set(this.connection, "sessionId", "ABC123");
		this.connection.init();
	}

	@Test
	public void send_frame() {
		final HeartBeatMonitor heartBeatMonitor = mock(HeartBeatMonitor.class);
		ReflectionUtil.set(this.connection, "heartBeatMonitor", heartBeatMonitor);
		final Frame frame = mock(Frame.class);
		when(frame.getCommand()).thenReturn(Command.MESSAGE);

		this.connection.sendToClient(frame);

		verify(heartBeatMonitor).resetSend();
		verify(frame).getCommand();
		verify(this.log).info("Sending message to client. [sessionId={},command={}]", "ABC123", Command.MESSAGE);
		verify(this.brokerMessageEvent).fire(any(Message.class));
		verifyNoMoreInteractions(heartBeatMonitor, frame);
	}

	@Test
	public void connect() throws JMSException, LoginException {
		ReflectionUtil.set(this.connection, "sessionId", null); // every other test needs it set!
		final HeartBeatMonitor heartBeatMonitor = mock(HeartBeatMonitor.class);
		ReflectionUtil.set(this.connection, "heartBeatMonitor", heartBeatMonitor);
		final Frame frame = Frame.connect("myhost.com", "1.0").build();
		final Message messageEvent = new Message("ABC123", frame);
		final javax.jms.Connection jmsConnection = mock(javax.jms.Connection.class);
		when(this.connectionFactory.createConnection(null, null)).thenReturn(jmsConnection);
		when(this.securityCtx.isUnsatisfied()).thenReturn(true);

		this.connection.connect(messageEvent);

		verify(this.log).info("Connecting... [sessionId={}]", "ABC123");
		verify(this.securityCtx).isUnsatisfied();
		verify(this.connectionFactory).createConnection(null, null);
		verify(heartBeatMonitor).resetSend();
		verify(this.log).info("Starting JMS connection... [sessionId={}]", "ABC123");
		verify(jmsConnection).setClientID("ABC123");
		verify(jmsConnection).start();
		verify(this.log).info("Sending message to client. [sessionId={},command={}]", "ABC123", Command.CONNECTED);
		verify(this.brokerMessageEvent).fire(any(Message.class));
		verifyNoMoreInteractions(heartBeatMonitor, jmsConnection);
	}

	@Test
	public void on_wrongSession() {
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("Session identifier mismatch! [expected=ABC123,actual=Another]");

		this.connection.on(new Message("Another", Frame.HEART_BEAT));
	}

	@Test
	public void on_CONNECT() {
		final Message messageEvent = new Message("ABC123", Frame.connect("myhost.com", "1.0").build());

		IllegalArgumentException expected = null;
		try {
			this.connection.on(messageEvent);
			fail("IllegalArgumentException expected!");
		} catch (IllegalArgumentException e) {
			expected = e;
		}
		assertEquals("CONNECT not supported! [ABC123]", expected.getMessage());
	}

	@Test
	public void on_DISCONNECT() {
		final Message messageEvent = new Message("ABC123", Frame.disconnect().build());

		IllegalArgumentException expected = null;
		try {
			this.connection.on(messageEvent);
			fail("IllegalArgumentException expected!");
		} catch (IllegalArgumentException e) {
			expected = e;                                                                                                                
		}
		assertEquals("DISCONNECT not supported! [ABC123]", expected.getMessage());
	}

	@Test
	public void on_SEND() throws JMSException {
		final Session session = mock(Session.class);
		ReflectionUtil.set(this.connection, "session", session);

		final Frame frame = Frame.send("/there", null, "{}").build();
		this.connection.on(new Message("ABC123", frame));

		verify(session).sendToBroker(frame);
		verify(this.log).info("Message received. [sessionId={},command={}]", "ABC123", Command.SEND);
		verifyNoMoreInteractions(session);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void on_ACK() throws JMSException {
		final javax.jms.Message msg = mock(javax.jms.Message.class);
		ReflectionUtil.get(this.connection, "ackMessages", Map.class).put("1", msg);

		this.connection.on(new Message("ABC123", Frame.builder(Command.ACK).header(Standard.ID, "1").build()));

		verify(this.log).info("Message received. [sessionId={},command={}]", "ABC123", Command.ACK);
		verify(msg).acknowledge();
		verifyNoMoreInteractions(msg);
	}

	@Test
	public void on_ACK_noExist() {
		IllegalStateException expected = null;
		try {
			this.connection.on(new Message("ABC123", Frame.builder(Command.ACK).header(Standard.ID, "1").build()));
		} catch (IllegalStateException e) {
			expected = e;                                                                                                                
		}
		assertEquals("No such message to ACK! [1]", expected.getMessage());
		verify(this.log).info("Message received. [sessionId={},command={}]", "ABC123", Command.ACK);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void on_NACK() {
		final javax.jms.Message msg = mock(javax.jms.Message.class);
		ReflectionUtil.get(this.connection, "ackMessages", Map.class).put("1", msg);

		this.connection.on(new Message("ABC123", Frame.builder(Command.NACK).header(Standard.ID, "1").build()));

		verify(this.log).info("Message received. [sessionId={},command={}]", "ABC123", Command.NACK);
		verify(this.log).warn("NACK recieved, but no JMS equivalent! [{}]", "1");
		verifyNoMoreInteractions(msg);
	}

	@Test
	public void on_NACK_noExist() {
		IllegalStateException expected = null;
		try {
			this.connection.on(new Message("ABC123", Frame.builder(Command.NACK).header(Standard.ID, "1").build()));
		} catch (IllegalStateException e) {
			expected = e;                                                                                                                
		}
		assertNotNull(expected);
		assertEquals("No such message to NACK! [1]", expected.getMessage());
		verify(this.log).info("Message received. [sessionId={},command={}]", "ABC123", Command.NACK);
	}

	@Test
	public void on_BEGIN() throws JMSException {
		final Session txSession = mock(Session.class);
		when(this.factory.toSession(this.connection, true, javax.jms.Session.SESSION_TRANSACTED)).thenReturn(txSession);

		this.connection.on(new Message("ABC123", Frame.builder(Command.BEGIN).header(Standard.TRANSACTION, "1").build()));

		assertEquals(txSession, ReflectionUtil.get(this.connection, "txSessions", Map.class).get("1"));

		verify(this.log).info("Message received. [sessionId={},command={}]", "ABC123", Command.BEGIN);
		verify(this.factory).toSession(this.connection, true, javax.jms.Session.SESSION_TRANSACTED);
		verifyNoMoreInteractions(txSession);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void on_BEGIN_alreadyExists() {
		final Session txSession = mock(Session.class);
		ReflectionUtil.get(this.connection, "txSessions", Map.class).put("1", txSession);

		IllegalStateException expected = null;
		try {
			this.connection.on(new Message("ABC123", Frame.builder(Command.BEGIN).header(Standard.TRANSACTION, "1").build()));
		} catch (IllegalStateException e) {
			expected = e;
		}
		assertEquals("Transaction already started! [1]", expected.getMessage());

		verify(this.log).info("Message received. [sessionId={},command={}]", "ABC123", Command.BEGIN);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void on_COMMIT() throws JMSException {
		final Session txSession = mock(Session.class);
		ReflectionUtil.get(this.connection, "txSessions", Map.class).put("1", txSession);

		this.connection.on(new Message("ABC123", Frame.builder(Command.COMMIT).header(Standard.TRANSACTION, "1").build()));

		verify(this.log).info("Message received. [sessionId={},command={}]", "ABC123", Command.COMMIT);
		verify(txSession).commit();
		verifyNoMoreInteractions(txSession);
	}

	@Test
	public void on_COMMIT_notExists() {
		IllegalStateException expected = null;
		try {
			this.connection.on(new Message("ABC123", Frame.builder(Command.COMMIT).header(Standard.TRANSACTION, "1").build()));
		} catch (IllegalStateException e) {
			expected = e;
		}
		assertEquals("Transaction session does not exists! [1]", expected.getMessage());

		verify(this.log).info("Message received. [sessionId={},command={}]", "ABC123", Command.COMMIT);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void on_ABORT() throws JMSException {
		final Session txSession = mock(Session.class);
		ReflectionUtil.get(this.connection, "txSessions", Map.class).put("1", txSession);

		this.connection.on(new Message("ABC123", Frame.builder(Command.ABORT).header(Standard.TRANSACTION, "1").build()));

		verify(this.log).info("Message received. [sessionId={},command={}]", "ABC123", Command.ABORT);
		verify(txSession).rollback();
		verifyNoMoreInteractions(txSession);

		verify(this.log).info("Message received. [sessionId={},command={}]", "ABC123", Command.ABORT);
	}

	@Test
	public void on_ABORT_notExists() {
		IllegalStateException expected = null;
		try {
			this.connection.on(new Message("ABC123", Frame.builder(Command.ABORT).header(Standard.TRANSACTION, "1").build()));
		} catch (IllegalStateException e) {
			expected = e;
		}
		assertEquals("Transaction session does not exists! [1]", expected.getMessage());

		verify(this.log).info("Message received. [sessionId={},command={}]", "ABC123", Command.ABORT);
	}

	@Test
	public void on_SUBSCRIBE() throws JMSException {
		final Session session = mock(Session.class);
		ReflectionUtil.set(this.connection, "session", session);
		final Destination destination = mock(Destination.class);
		when(session.toDestination("/dest")).thenReturn(destination);
		when(session.getConnection()).thenReturn(this.connection);
		final MessageConsumer consumer = mock(MessageConsumer.class);
		when(session.createConsumer(eq(destination), any(String.class))).thenReturn(consumer);

		this.connection.on(new Message("ABC123", Frame.subscribe("1", "/dest").build()));

		verify(this.log).info("Message received. [sessionId={},command={}]", "ABC123", Command.SUBSCRIBE);
		verify(session).toDestination("/dest");
		verify(session).getConnection();
		verify(session).createConsumer(destination, "session IS NULL OR session = 'ABC123'");
		verify(consumer).setMessageListener(any(MessageListener.class));
		verifyNoMoreInteractions(session, destination, consumer);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void on_SUBSCRIBE_alreadyExists() {
		final Subscription subscription = mock(Subscription.class);
		ReflectionUtil.get(this.connection, "subscriptions", Map.class).put("1", subscription);

		IllegalStateException expected = null;
		try {
			this.connection.on(new Message("ABC123", Frame.subscribe("1", "/dest").build()));
		} catch (IllegalStateException e) {
			expected = e;
		}
		assertEquals("Subscription already exists! [1]", expected.getMessage());

		verify(this.log).info("Message received. [sessionId={},command={}]", "ABC123", Command.SUBSCRIBE);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void on_UNSUBSCRIBE() {
		final Subscription subscription = mock(Subscription.class);
		ReflectionUtil.get(this.connection, "subscriptions", Map.class).put("1", subscription);

		this.connection.on(new Message("ABC123", Frame.builder(Command.UNSUBSCRIBE).subscription("1").build()));

		verify(this.log).info("Message received. [sessionId={},command={}]", "ABC123", Command.UNSUBSCRIBE);
	}

	@Test
	public void on_UNSUBSCRIBE_noExist() {
		IllegalStateException expected = null;
		try {
			this.connection.on(new Message("ABC123", Frame.builder(Command.UNSUBSCRIBE).subscription("1").build()));
		} catch (IllegalStateException e) {
			expected = e;
		}
		assertEquals("Subscription does not exist! [1]", expected.getMessage());

		verify(this.log).info("Message received. [sessionId={},command={}]", "ABC123", Command.UNSUBSCRIBE);
	}

	@Test
	public void addAckMessage() throws JMSException {
		final javax.jms.Message msg = mock(javax.jms.Message.class);
		when(msg.getJMSMessageID()).thenReturn("foo");

		this.connection.addAckMessage(msg);

		assertEquals(msg, ReflectionUtil.<Map<String, Message>>get(this.connection, "ackMessages").get("foo"));

		verify(msg).getJMSMessageID();
		verifyNoMoreInteractions(msg);
	}

	@Test
	public void close_closeReason() throws IOException, JMSException {
		final HeartBeatMonitor heartBeatMonitor = mock(HeartBeatMonitor.class);
		ReflectionUtil.set(this.connection, "heartBeatMonitor", heartBeatMonitor);
		final javax.jms.Connection jmsConnection = mock(javax.jms.Connection.class);
		ReflectionUtil.set(this.connection, "delegate", jmsConnection);
		final CloseReason reason = new CloseReason(CloseCodes.CANNOT_ACCEPT, "Aggghhh!");

		this.connection.close(reason);

		verify(this.log).info("Closing connection. [sessionId={},code={},reason={}]", "ABC123", CloseCodes.CANNOT_ACCEPT.getCode(), "Aggghhh!");
		verify(jmsConnection).close();
		verify(heartBeatMonitor).close();
		verifyNoMoreInteractions(heartBeatMonitor, jmsConnection);
	}

	@After
	public void after() {
		verifyNoMoreInteractions(this.log,
				this.beanManager,
				this.relay,
				this.connectionFactory,
				this.factory,
				this.scheduler,
				this.brokerMessageEvent,
				this.wsSessionProvider,
				this.wsSession,
				this.securityCtx);
	}
}
