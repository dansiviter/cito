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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import cito.stomp.Frame;

/**
 * Unit test for {@link Subscription}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Jul 2016]
 */
public class SubscriptionTest {
	@Rule
	public MockitoRule mockito = MockitoJUnit.rule();

	@Mock
	private Session session;
	@Mock
	private Destination destination;
	@Mock
	private AbstractConnection connection;
	@Mock
	private MessageConsumer messageConsumer;

	private Subscription subscription;

	@Before
	public void before() throws JMSException {
		final Frame frame = Frame.subscribe("id", "/foo").build();
		when(this.session.toDestination(eq("/foo"))).thenReturn(this.destination);
		when(this.session.getConnection()).thenReturn(connection);
		when(this.session.createConsumer(eq(this.destination), anyString())).thenReturn(this.messageConsumer);
		this.subscription = new Subscription(this.session, "id", frame);
	}

	@Test
	public void onMessage() throws JMSException, IOException {
		final Message message = mock(Message.class);

		this.subscription.onMessage(message);

		verify(this.session).send(message, this.subscription);
		verify(this.session).getAcknowledgeMode();
		verifyNoMoreInteractions(message);
	}

	@Test
	public void close() throws JMSException {
		this.subscription.close();

		verify(this.messageConsumer).close();
	}

	@After
	public void after() throws JMSException {
		verify(this.session).toDestination(eq("/foo"));
		verify(this.session).getConnection();
		verify(this.connection).getSessionId();
		verify(this.session).createConsumer(eq(this.destination), anyString());
		verify(this.messageConsumer).setMessageListener(this.subscription);
		verifyNoMoreInteractions(this.session, this.destination, this.connection, messageConsumer);
	}
}
