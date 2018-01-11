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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Optional;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.ws.rs.core.MultivaluedHashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import cito.ReflectionUtil;
import cito.stomp.Frame;
import cito.stomp.Header;
import cito.stomp.Header.Standard;

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
		assertEquals(jmsSession, ReflectionUtil.get(session, "delegate"));

		verify(conn).getDelegate();
		verify(jmsConn).createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
		verifyNoMoreInteractions(conn, jmsConn, jmsSession);
	}

	@Test
	public void toDestination_queue() throws JMSException {
		final javax.jms.Session session = mock(javax.jms.Session.class);

		this.factory.toDestination(session, "queue/foo");

		verify(session).createQueue("foo");
		verifyNoMoreInteractions(session);
	}

	@Test
	public void toDestination_topic() throws JMSException {
		final javax.jms.Session session = mock(javax.jms.Session.class);

		this.factory.toDestination(session, "topic/foo");

		verify(session).createTopic("foo");
		verifyNoMoreInteractions(session);
	}

	@Test
	public void fromDestination_queue() throws JMSException {
		final Queue q = mock(Queue.class);
		when(q.getQueueName()).thenReturn("foo");

		final String output = this.factory.fromDestination(q);
		assertEquals("queue/foo", output);

		verify(q).getQueueName();
		verifyNoMoreInteractions(q);
	}

	@Test
	public void fromDestination_topic() throws JMSException {
		final Topic t = mock(Topic.class);
		when(t.getTopicName()).thenReturn("foo");

		final String output = this.factory.fromDestination(t);
		assertEquals("topic/foo", output);

		verify(t).getTopicName();
		verifyNoMoreInteractions(t);
	}

	@Test
	public void toMessage_bytes() throws JMSException {
		final javax.jms.Session session = mock(javax.jms.Session.class);
		final Frame frame = mock(Frame.class);
		final ByteBuffer buffer = ByteBuffer.wrap(new byte[0]).asReadOnlyBuffer();
		when(frame.body()).thenReturn(Optional.of(buffer));
		when(frame.headers()).thenReturn(new MultivaluedHashMap<>());
		when(frame.contains(Standard.CONTENT_LENGTH)).thenReturn(true);
		final BytesMessage message = mock(BytesMessage.class);
		when(session.createBytesMessage()).thenReturn(message);

		this.factory.toMessage(session, frame);

		verify(frame).body();
		verify(frame, times(2)).headers();
		verify(frame).contains(Standard.CONTENT_LENGTH);
		verify(session).createBytesMessage();
		verify(message).setJMSCorrelationID(null);
		verify(message).writeBytes(new byte[0]);
		verifyNoMoreInteractions(session, frame, message);
	}

	@Test
	public void toMessage_text() throws JMSException {
		final javax.jms.Session session = mock(javax.jms.Session.class);
		final Frame frame = mock(Frame.class);
		final ByteBuffer buffer = ByteBuffer.wrap(new byte[0]).asReadOnlyBuffer();
		when(frame.body()).thenReturn(Optional.of(buffer));
		when(frame.headers()).thenReturn(new MultivaluedHashMap<>());
		when(frame.contains(Standard.CONTENT_LENGTH)).thenReturn(false);
		final TextMessage message = mock(TextMessage.class);
		when(session.createTextMessage("")).thenReturn(message);

		this.factory.toMessage(session, frame);

		verify(frame).body();
		verify(frame, times(2)).headers();
		verify(frame).contains(Standard.CONTENT_LENGTH);
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
		verify(message).getStringProperty(Standard.CONTENT_TYPE.value());
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
		verify(message).getStringProperty(Standard.CONTENT_TYPE.value());
		verify(message).getBodyLength();
		verify(message).readBytes(new byte[0]);
		verifyNoMoreInteractions(message);
	}

	@Test
	public void toJmsKey() {
		assertEquals("my_HYPHEN_COMPLEX_DOT_key", Factory.toJmsKey(Header.valueOf("my-COMPLEX.key")));
	}

	@Test
	public void toStompKey() {
		assertEquals("my-COMPLEX.key", Factory.toStompKey("my_HYPHEN_COMPLEX_DOT_key").value());
	}
}
