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

import static cito.stomp.jms.SystemConnection.SESSION_ID;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.enterprise.context.ApplicationScoped;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import cito.ReflectionUtil;
import cito.event.Message;
import cito.stomp.Frame;

/**
 * Unit tests for {@link SystemConnection}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [14 Apr 2017]
 */
@RunWith(MockitoJUnitRunner.class)
public class SystemConnectionTest {
	@Mock
	private Logger log;
	@Mock
	private ConnectionFactory connectionFactory;
	@Mock
	private Factory factory;
	@InjectMocks
	private SystemConnection connection;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void scope() {
		assertNotNull(ReflectionUtil.getAnnotation(SystemConnection.class, ApplicationScoped.class));
	}

	@Test
	public void init() throws JMSException {
		final Connection jmsConnection = Mockito.mock(Connection.class);
		when(this.connectionFactory.createConnection(SESSION_ID, null)).thenReturn(jmsConnection);

		this.connection.init();

		verify(this.connectionFactory).createConnection(SESSION_ID, null);
		verify(this.log).info("Starting JMS connection... [sessionId={}]", SESSION_ID);
		verify(jmsConnection).setClientID(SESSION_ID);
		verify(jmsConnection).start();
		verifyNoMoreInteractions(jmsConnection);
	}

	@Test
	public void sendToClient() {
		this.expectedException.expect(UnsupportedOperationException.class);
		this.expectedException.expectMessage("Cannot sent to client from system connection!");

		this.connection.sendToClient(null);
	}

	@Test
	public void on_message() throws JMSException {
		final Message msg = mock(Message.class);
		final Session session = mock(Session.class);
		when(this.factory.toSession(this.connection, false, javax.jms.Session.AUTO_ACKNOWLEDGE)).thenReturn(session);
		when(msg.sessionId()).thenReturn(SESSION_ID);
		final Frame frame = mock(Frame.class);
		when(msg.frame()).thenReturn(frame);

		this.connection.on(msg);

		verify(this.factory).toSession(this.connection, false, javax.jms.Session.AUTO_ACKNOWLEDGE);
		verify(msg).sessionId();
		verify(this.log).debug("Message event. [sessionId={}]", SESSION_ID);
		verify(session).sendToBroker(frame);
		verify(msg).frame();
		verifyNoMoreInteractions(msg, session, frame);
	}

	@Test
	public void on_message_incorrectSessionId() throws JMSException {
		this.expectedException.expect(IllegalArgumentException.class);
		this.expectedException.expectMessage("Session identifier mismatch! [expected=" + SESSION_ID + " OR null,actual=another]");

		final Message msg = mock(Message.class);
		final Session session = mock(Session.class);
		when(this.factory.toSession(this.connection, false, javax.jms.Session.AUTO_ACKNOWLEDGE)).thenReturn(session);
		when(msg.sessionId()).thenReturn("another");

		this.connection.on(msg);

		verify(this.factory).toSession(this.connection, false, javax.jms.Session.AUTO_ACKNOWLEDGE);
		verify(msg).sessionId();
		verifyNoMoreInteractions(msg, session);
	}

	@After
	public void after() {
		verifyNoMoreInteractions(this.log, this.connectionFactory);
	}
}
