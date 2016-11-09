package cito.stomp.jms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import javax.enterprise.inject.spi.BeanManager;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;

import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import cito.ReflectionUtil;
import cito.stomp.Command;
import cito.stomp.Frame;
import cito.stomp.HeartBeatMonitor;
import cito.stomp.server.event.MessageEvent;

/**
 * Unit tests for {@link Connection}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Jul 2016]
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectionTest {
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

	@InjectMocks
	private Connection connection;

	@Before
	public void before() {
		ReflectionUtil.set(this.connection, "sessionId", "ABC123");
	}

	@Test
	public void init() {
		this.connection.init();
	}

	@Test
	public void send_frame() {
		final HeartBeatMonitor heartBeatMonitor = mock(HeartBeatMonitor.class);
		ReflectionUtil.set(this.connection, "heartBeatMonitor", heartBeatMonitor);
		final Frame frame = mock(Frame.class);
		when(frame.getCommand()).thenReturn(Command.MESSAGE);

		this.connection.send(frame);

		verify(heartBeatMonitor).resetSend();
		verify(frame).getCommand();
		verify(this.log).info("Senging message to client. [sessionId={},command={}]", "ABC123", Command.MESSAGE);
		verify(this.relay).send(any(MessageEvent.class));
		verifyNoMoreInteractions(heartBeatMonitor, frame);
	}

	@Test
	public void connect() throws JMSException {
		ReflectionUtil.set(this.connection, "sessionId", null); // every other test needs it set!
		final HeartBeatMonitor heartBeatMonitor = mock(HeartBeatMonitor.class);
		ReflectionUtil.set(this.connection, "heartBeatMonitor", heartBeatMonitor);
		final MessageEvent messageEvent = new MessageEvent("ABC123", Frame.connect("myhost.com", "1.0").build());
		final javax.jms.Connection jmsConnection = mock(javax.jms.Connection.class);
		when(this.connectionFactory.createConnection()).thenReturn(jmsConnection);

		this.connection.connect(messageEvent);

		verify(this.log).info("Connecting... [sessionId={}]", "ABC123");
		verify(this.connectionFactory).createConnection();
		verify(heartBeatMonitor).resetSend();
		verify(this.log).info("Starting JMS connection... [sessionId={}]", "ABC123");
		verify(jmsConnection).setClientID("ABC123");
		verify(jmsConnection).start();
		verify(this.log).info("Senging message to client. [sessionId={},command={}]", "ABC123", Command.CONNECTED);
		verify(this.relay).send(any(MessageEvent.class));
		verifyNoMoreInteractions(heartBeatMonitor, jmsConnection);
	}

	@Test(expected = IllegalArgumentException.class)
	public void on_wrongSession() {
		final MessageEvent messageEvent = new MessageEvent("Another", Frame.HEART_BEAT);

		this.connection.on(messageEvent);

	}

	@Test
	public void on_connect() {
		final MessageEvent messageEvent = new MessageEvent("ABC123", Frame.connect("myhost.com", "1.0").build());

		IllegalArgumentException expected = null;
		try {
			this.connection.on(messageEvent);
			fail("IllegalArgumentException expected!");
		} catch (IllegalArgumentException e) {
			expected = e;
		}
		assertNotNull(expected);
		assertEquals("CONNECT not supported! [ABC123]", expected.getMessage());
	}

	@Test
	public void on_disconnect() {
		final MessageEvent messageEvent = new MessageEvent("ABC123", Frame.disconnect().build());

		IllegalArgumentException expected = null;
		try {
			this.connection.on(messageEvent);
			fail("IllegalArgumentException expected!");
		} catch (IllegalArgumentException e) {
			expected = e;                                                                                                                
		}
		assertNotNull(expected);
		assertEquals("DISCONNECT not supported! [ABC123]", expected.getMessage());
	}


	@Test
	public void addAckMessage() throws JMSException {
		final Message msg = mock(Message.class);
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
				this.scheduler);
	}
}
