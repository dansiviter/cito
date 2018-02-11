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
package cito.server;

import static cito.ReflectionUtil.findField;
import static cito.stomp.Command.SEND;
import static java.util.Collections.emptyMap;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.security.Principal;
import java.util.Arrays;
import java.util.LinkedHashSet;

import javax.enterprise.event.Event;
import javax.websocket.Session;
import javax.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;

import cito.annotation.FromServer;
import cito.event.Message;
import cito.ext.Serialiser;

/**
 * Unit test for {@link MessagingSupport}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Jul 2016]
 */
@RunWith(MockitoJUnitRunner.class)
public class MessagingSupportTest {
	@Mock
	private Logger log;
	@Mock
	private Event<Message> msgEvent;
	@Mock
	private SessionRegistry registry;
	@Mock
	private Serialiser serialiser;
	@Mock
	private Principal principal;

	@InjectMocks
	private MessagingSupport support;

	@Before
	public void before() throws IOException {
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				((OutputStream) invocation.getArgument(3)).write("payload".getBytes());
				return null;
			}
		}).when(this.serialiser).writeTo(any(), any(), any(), any());
	}

	@Test
	public void msgEvent_annotations() {
		final Field field = findField(MessagingSupport.class, "msgEvent", Event.class);
		assertNotNull(field.getAnnotation(FromServer.class));
	}

	@Test
	public void broadcast_destination_payload() throws IOException {
		this.support.broadcast("destination", "payload", emptyMap());

		final ArgumentCaptor<Message> eventCaptor = ArgumentCaptor.forClass(Message.class);
		verify(this.msgEvent).fire(eventCaptor.capture());
		assertMessage(eventCaptor.getValue(), null, "destination", null);

		verify(this.log).debug("Broadcasting... [destination={}]", "destination");
		verify(this.serialiser).writeTo(any(), any(Class.class), isNull(), any(OutputStream.class));
	}

	@Test
	public void broadcast_destination_mediaType_payload() throws IOException {
		this.support.broadcast("destination", "payload", TEXT_PLAIN_TYPE, emptyMap());

		final ArgumentCaptor<Message> eventCaptor = ArgumentCaptor.forClass(Message.class);
		verify(this.msgEvent).fire(eventCaptor.capture());
		final Message msgEvent = eventCaptor.getValue();
		assertEquals(SEND, msgEvent.frame().command());
		assertNull(msgEvent.sessionId());
		assertFalse(msgEvent.frame().session().isPresent());
		assertEquals("destination", msgEvent.frame().destination().get());
		assertEquals("text/plain", msgEvent.frame().contentType().get().toString());

		verify(this.log).debug("Broadcasting... [destination={}]", "destination");
		verify(this.serialiser).writeTo(any(), any(Class.class), eq(TEXT_PLAIN_TYPE), any(OutputStream.class));
	}

	@Test
	public void broadcastTo_destination_payload() throws IOException {
		final Session session0 = mock(Session.class);
		when(session0.getId()).thenReturn("session0");
		final Session session1 = mock(Session.class);
		when(session1.getId()).thenReturn("session1");
		when(this.registry.getSessions(this.principal)).thenReturn(new LinkedHashSet<>(Arrays.asList(session0, session1)));

		this.support.broadcastTo(principal, "destination", "payload", emptyMap());

		final ArgumentCaptor<Message> eventCaptor = ArgumentCaptor.forClass(Message.class);
		verify(this.msgEvent, times(2)).fire(eventCaptor.capture());

		assertMessage(eventCaptor.getAllValues().get(0), "session0", "destination", null);
		assertMessage(eventCaptor.getAllValues().get(1), "session1", "destination", null);

		verify(this.log).debug("Sending... [sessionId={},destination={}]", "session0", "destination");
		verify(this.log).debug("Sending... [sessionId={},destination={}]", "session1", "destination");
		verify(this.registry).getSessions(principal);
		verify(this.serialiser, times(2)).writeTo(any(), any(Class.class), isNull(), any(OutputStream.class));
		verify(session0).getId();
		verify(session1).getId();
		verifyNoMoreInteractions(session0, session1);
	}

	@Test
	public void broadcastTo() throws IOException {
		final Session session0 = mock(Session.class);
		when(session0.getId()).thenReturn("session0");
		final Session session1 = mock(Session.class);
		when(session1.getId()).thenReturn("session1");
		when(this.registry.getSessions(principal)).thenReturn(new LinkedHashSet<>(Arrays.asList(session0, session1)));

		this.support.broadcastTo(principal, "destination", "payload", TEXT_PLAIN_TYPE, emptyMap());

		final ArgumentCaptor<Message> eventCaptor = ArgumentCaptor.forClass(Message.class);
		verify(this.msgEvent, times(2)).fire(eventCaptor.capture());

		final Message msgEvent1 = eventCaptor.getAllValues().get(1);
		assertMessage(eventCaptor.getAllValues().get(0), "session0", "destination", TEXT_PLAIN_TYPE);
		assertMessage(eventCaptor.getAllValues().get(1), "session1", "destination", TEXT_PLAIN_TYPE);

		assertEquals("session1", msgEvent1.frame().session().get());
		assertEquals("destination", msgEvent1.frame().destination().get());
		assertEquals("text/plain", msgEvent1.frame().contentType().get().toString());

		verify(this.log).debug("Sending... [sessionId={},destination={}]", "session0", "destination");
		verify(this.log).debug("Sending... [sessionId={},destination={}]", "session1", "destination");
		verify(this.registry).getSessions(principal);
		verify(this.serialiser, times(2)).writeTo(any(), any(Class.class), eq(TEXT_PLAIN_TYPE), any(OutputStream.class));
		verify(session0).getId();
		verify(session1).getId();
		verifyNoMoreInteractions(session0, session1);
	}

	@Test
	public void sendTo_destination_payload() throws IOException {
		this.support.sendTo("sessionId", "destination", "payload", emptyMap());

		final ArgumentCaptor<Message> eventCaptor = ArgumentCaptor.forClass(Message.class);
		verify(this.msgEvent).fire(eventCaptor.capture());
		assertMessage(eventCaptor.getValue(), "sessionId", "destination", null);

		verify(this.log).debug("Sending... [sessionId={},destination={}]", "sessionId", "destination");
		verify(this.serialiser).writeTo(any(), any(Class.class), isNull(), any(OutputStream.class));
	}

	@Test
	public void sendTo_destination_mediaType_payload() throws IOException {
		this.support.sendTo("sessionId", "destination", "payload", TEXT_PLAIN_TYPE, emptyMap());

		final ArgumentCaptor<Message> eventCaptor = ArgumentCaptor.forClass(Message.class);
		verify(this.msgEvent).fire(eventCaptor.capture());
		assertMessage(eventCaptor.getValue(), "sessionId", "destination", TEXT_PLAIN_TYPE);

		verify(this.log).debug("Sending... [sessionId={},destination={}]", "sessionId", "destination");
		verify(this.serialiser).writeTo(any(), any(Class.class), eq(TEXT_PLAIN_TYPE), any(OutputStream.class));
	}

	@After
	public void after() {
		verifyNoMoreInteractions(this.log, this.msgEvent, this.registry, this.serialiser, this.principal);
	}


	// --- Static Methods ---

	/**
	 * 
	 * @param msg
	 * @param sessionId
	 * @param destination
	 * @param contentType
	 */
	private static void assertMessage(Message msg, String sessionId, String destination, MediaType contentType) {
		assertNull(msg.sessionId());
		assertEquals(SEND, msg.frame().command());
		if (sessionId == null) {
			assertFalse(msg.frame().session().isPresent());
		} else {
			assertEquals(sessionId, msg.frame().session().get());
		}
		assertEquals(destination, msg.frame().destination().get());
		if (contentType == null) {
			assertFalse(msg.frame().contentType().isPresent());
		} else {
			assertEquals(contentType, msg.frame().contentType().get());
		}
	}
}
