package cito.stomp.jms;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.ws.rs.core.MultivaluedHashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import cito.stomp.Frame;
import cito.stomp.Headers;

/**
 * Unit tests for {@link Factory}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Oct 2016]
 */
@RunWith(MockitoJUnitRunner.class)
public class FactoryTest {
	private Factory factory;

	@Before
	public void before() {
		this.factory = new Factory();
	}

	@Test
	public void toSession() throws JMSException {
		final AbstractConnection conn = mock(AbstractConnection.class);
		final javax.jms.Connection jmsConn = mock(javax.jms.Connection.class);
		when(conn.getDelegate()).thenReturn(jmsConn);
		final javax.jms.Session jmsSession = mock(javax.jms.Session.class);
		when(jmsConn.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE)).thenReturn(jmsSession);

		final Session session = this.factory.toSession(conn, false, javax.jms.Session.AUTO_ACKNOWLEDGE);
		assertEquals(jmsSession, session.getDelegate());

		verify(conn).getDelegate();
		verify(jmsConn).createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
		verifyNoMoreInteractions(conn, jmsConn, jmsSession);
	}

	@Test
	public void toDestination_queue() throws JMSException {
		final javax.jms.Session session = mock(javax.jms.Session.class);

		this.factory.toDestination(session, "/queue/foo");

		verify(session).createQueue("foo");
		verifyNoMoreInteractions(session);
	}

	@Test
	public void toDestination_topic() throws JMSException {
		final javax.jms.Session session = mock(javax.jms.Session.class);

		this.factory.toDestination(session, "/topic/foo");

		verify(session).createTopic("foo");
		verifyNoMoreInteractions(session);
	}

	@Test
	public void fromDestination_queue() throws JMSException {
		final Queue q = mock(Queue.class);
		when(q.getQueueName()).thenReturn("foo");

		final String output = this.factory.fromDestination(q);
		assertEquals("/queue/foo", output);

		verify(q).getQueueName();
		verifyNoMoreInteractions(q);
	}

	@Test
	public void fromDestination_topic() throws JMSException {
		final Topic t = mock(Topic.class);
		when(t.getTopicName()).thenReturn("foo");

		final String output = this.factory.fromDestination(t);
		assertEquals("/topic/foo", output);

		verify(t).getTopicName();
		verifyNoMoreInteractions(t);
	}

	@Test
	public void toMessage_bytes() throws JMSException {
		final javax.jms.Session session = mock(javax.jms.Session.class);
		final Frame frame = mock(Frame.class);
		final ByteBuffer buffer = ByteBuffer.wrap(new byte[0]);
		when(frame.getBody()).thenReturn(buffer);
		when(frame.getHeaders()).thenReturn(new MultivaluedHashMap<>());
		when(frame.containsHeader(Headers.CONTENT_LENGTH)).thenReturn(true);
		final BytesMessage message = mock(BytesMessage.class);
		when(session.createBytesMessage()).thenReturn(message);

		this.factory.toMessage(session, frame);

		verify(frame).getBody();
		verify(frame, times(2)).getHeaders();
		verify(frame).containsHeader(Headers.CONTENT_LENGTH);
		verify(session).createBytesMessage();
		verify(message).setJMSCorrelationID(null);
		verify(message).writeBytes(new byte[0]);
		verifyNoMoreInteractions(session, frame, message);
	}

	@Test
	public void toMessage_text() throws JMSException {
		final javax.jms.Session session = mock(javax.jms.Session.class);
		final Frame frame = mock(Frame.class);
		final ByteBuffer buffer = ByteBuffer.wrap(new byte[0]);
		when(frame.getBody()).thenReturn(buffer);
		when(frame.getHeaders()).thenReturn(new MultivaluedHashMap<>());
		when(frame.containsHeader(Headers.CONTENT_LENGTH)).thenReturn(false);
		final TextMessage message = mock(TextMessage.class);
		when(session.createTextMessage("")).thenReturn(message);

		this.factory.toMessage(session, frame);

		verify(frame).getBody();
		verify(frame, times(2)).getHeaders();
		verify(frame).containsHeader(Headers.CONTENT_LENGTH);
		verify(session).createTextMessage("");
		verify(message).setJMSCorrelationID(null);
		verifyNoMoreInteractions(session, frame, message);
	}


	@Test
	public void toFrame_textMessage() throws IOException, JMSException {
		final TextMessage message = mock(TextMessage.class);
		when(message.getPropertyNames()).thenReturn(Collections.enumeration(Collections.singleton("hello")));
		when(message.getText()).thenReturn("");

		this.factory.toFrame(message, "subscriptionId");

		verify(message).getPropertyNames();
		verify(message).getText();
		verify(message).getJMSMessageID();
		verify(message).getJMSDestination();
		verify(message).getJMSCorrelationID();
		verify(message).getJMSExpiration();
		verify(message).getJMSRedelivered();
		verify(message).getJMSPriority();
		verify(message).getJMSReplyTo();
		verify(message).getJMSTimestamp();
		verify(message).getJMSType();
		verify(message).getStringProperty("hello");
		verify(message).getStringProperty(Headers.CONTENT_TYPE);
		verify(message).getText();
		verifyNoMoreInteractions(message);
	}

	@Test
	public void toFrame_bytesMessage() throws IOException, JMSException {
		final BytesMessage message = mock(BytesMessage.class);
		when(message.getPropertyNames()).thenReturn(Collections.enumeration(Collections.singleton("hello")));

		this.factory.toFrame(message, "subscriptionId");

		verify(message).getPropertyNames();
		verify(message).getJMSMessageID();
		verify(message).getJMSDestination();
		verify(message).getJMSCorrelationID();
		verify(message).getJMSExpiration();
		verify(message).getJMSRedelivered();
		verify(message).getJMSPriority();
		verify(message).getJMSReplyTo();
		verify(message).getJMSTimestamp();
		verify(message).getJMSType();
		verify(message).getStringProperty("hello");
		verify(message).getStringProperty(Headers.CONTENT_TYPE);
		verify(message).getBodyLength();
		verify(message).readBytes(new byte[0]);
		verifyNoMoreInteractions(message);
	}

	@Test
	public void toJmsKey() {
		assertEquals("my_HYPHEN_COMPLEX_DOT_key", Factory.toJmsKey("my-COMPLEX.key"));
	}

	@Test
	public void toStompKey() {
		assertEquals("my-COMPLEX.key", Factory.toStompKey("my_HYPHEN_COMPLEX_DOT_key"));
	}
}
