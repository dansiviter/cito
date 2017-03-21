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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import cito.ReflectionUtil;
import cito.stomp.Frame;

/**
 * Unit tests for {@link Session}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Jul 2016]
 */
@RunWith(MockitoJUnitRunner.class)
public class SessionTest {
	@Mock
	private Connection conn;
	@Mock
	private javax.jms.Session delegate;
	@Mock
	private MessageProducer producer;
	@Mock
	private Factory factory;

	private Session session;

	@Before
	public void before() {
		this.session = new Session(this.conn, this.delegate, factory);
	}

	@Test
	public void send_frame() throws JMSException {
		ReflectionUtil.set(this.session, "producer", this.producer);
		final Frame frame = mock(Frame.class);
		when(frame.destination()).thenReturn("/here");
		final Message message = mock(Message.class);
		when(this.factory.toMessage(this.delegate, frame)).thenReturn(message);
		final Destination destination = mock(Destination.class);
		when(this.factory.toDestination(this.delegate, "/here")).thenReturn(destination);

		this.session.sendToBroker(frame);

		verify(frame).destination();
		verify(this.factory).toMessage(this.delegate, frame);
		verify(this.factory).toDestination(this.delegate, "/here");
		verify(this.producer).send(destination, message);
		verifyNoMoreInteractions(frame, message, destination);
	}

	@Test
	public void send_message() throws JMSException, IOException {
		ReflectionUtil.set(this.session, "producer", this.producer);
		final Frame frame = mock(Frame.class);
		when(frame.destination()).thenReturn("/here");
		final Message message = mock(Message.class);
		when(this.factory.toFrame(message, "subscriptionId")).thenReturn(frame);
		when(this.delegate.getAcknowledgeMode()).thenReturn(javax.jms.Session.AUTO_ACKNOWLEDGE);
		final Subscription subscription = mock(Subscription.class);
		when(subscription.getSubscriptionId()).thenReturn("subscriptionId");

		this.session.send(message, subscription);

		verify(this.delegate).getAcknowledgeMode();
		verify(subscription).getSubscriptionId();
		verify(this.factory).toFrame(message, "subscriptionId");
		verify(this.conn).sendToClient(frame);
		verifyNoMoreInteractions(message, subscription, message);
	}

	@Test
	public void send_message_ack() throws JMSException, IOException {
		ReflectionUtil.set(this.session, "producer", this.producer);
		final Frame frame = mock(Frame.class);
		when(frame.destination()).thenReturn("/here");
		final Message message = mock(Message.class);
		when(this.factory.toFrame(message, "subscriptionId")).thenReturn(frame);
		when(this.delegate.getAcknowledgeMode()).thenReturn(javax.jms.Session.CLIENT_ACKNOWLEDGE);
		final Subscription subscription = mock(Subscription.class);
		when(subscription.getSubscriptionId()).thenReturn("subscriptionId");

		this.session.send(message, subscription);

		verify(this.delegate).getAcknowledgeMode();
		verify(this.conn).addAckMessage(message);
		verify(subscription).getSubscriptionId();
		verify(this.factory).toFrame(message, "subscriptionId");
		verify(this.conn).sendToClient(frame);
		verifyNoMoreInteractions(message, subscription, message);
	}

	@After
	public void after() {
		verifyNoMoreInteractions(this.conn, this.delegate, this.factory, this.producer);
	}
}
