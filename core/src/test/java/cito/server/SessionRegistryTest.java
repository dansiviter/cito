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

import static cito.server.SessionRegistry.NULL_PRINCIPLE;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.websocket.EncodeException;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import cito.ReflectionUtil;
import cito.event.Message;
import cito.stomp.Command;
import cito.stomp.Frame;

/**
 * Unit tests for {@link SessionRegistry}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [15 Apr 2017]
 */
@RunWith(MockitoJUnitRunner.class)
public class SessionRegistryTest {
	@Mock
	private Logger log;

	@InjectMocks
	private SessionRegistry registry;

	@Test
	public void register() {
		final Session session = Mockito.mock(Session.class);
		when(session.getId()).thenReturn("sessionId");

		this.registry.register(session);

		assertTrue(getSessionMap().containsKey("sessionId"));
		assertTrue(getSessionMap().containsValue(session));
		assertTrue(getPrincipalSessionMap().get(NULL_PRINCIPLE).contains(session));

		verify(session).getId();
		verify(session).getUserPrincipal();
		verifyNoMoreInteractions(session);
	}

	@Test
	public void unregister() {
		final Session session = mock(Session.class);
		when(session.getId()).thenReturn("sessionId");
		getSessionMap().put("sessionId", session);
		getPrincipalSessionMap().put(NULL_PRINCIPLE, new HashSet<>(singleton(session)));

		this.registry.unregister(session);

		assertTrue(getSessionMap().isEmpty());
		assertTrue(getPrincipalSessionMap().isEmpty());

		verify(session).getId();
		verify(session).getUserPrincipal();
		verifyNoMoreInteractions(session);
	}

	@Test
	public void unregister_notRegistered() {
		final Session session = mock(Session.class);
		when(session.getId()).thenReturn("sessionId");

		IllegalArgumentException e = null;
		try {
			this.registry.unregister(session);
			fail("IllegalArgumentException expected!");
		} catch (IllegalArgumentException ex) {
			e = ex;
		}
		assertEquals("Session not registered! [sessionId]", e.getMessage());

		verify(session).getId();
		verifyNoMoreInteractions(session);
	}

	@Test
	public void getSession() {
		final Session session = Mockito.mock(Session.class);
		getSessionMap().put("sessionId", session);

		final Optional<Session> actual = this.registry.getSession("sessionId");
		assertNotNull(actual);
		assertTrue(actual.isPresent());

		verifyNoMoreInteractions(session);
	}

	@Test
	public void getSessions() {
		final Principal principal = mock(Principal.class);
		final Session session = Mockito.mock(Session.class);
		getPrincipalSessionMap().put(principal, Collections.singleton(session));

		final Set<Session> sessions = this.registry.getSessions(principal);
		assertFalse(sessions.isEmpty());
		assertTrue(sessions.contains(session));

		verifyNoMoreInteractions(principal, session);
	}

	@Test
	public void fromBroker() throws IOException, EncodeException {
		final Message msg = mock(Message.class);
		when(msg.sessionId()).thenReturn("sessionId");
		final Frame frame = mock(Frame.class);
		when(msg.frame()).thenReturn(frame);
		when(frame.getCommand()).thenReturn(Command.MESSAGE);
		final Session session = Mockito.mock(Session.class);
		getSessionMap().put("sessionId", session);
		getPrincipalSessionMap().put(NULL_PRINCIPLE, new HashSet<>(singleton(session)));
		final Basic basic = mock(Basic.class);
		when(session.getBasicRemote()).thenReturn(basic);

		this.registry.fromBroker(msg);

		verify(msg).sessionId();
		verify(msg).frame();
		verify(frame, times(2)).getCommand();
		verify(this.log).debug("Sending message to client. [sessionId={},command={}]", "sessionId", Command.MESSAGE);
		verify(session).getBasicRemote();
		verify(basic).sendObject(frame);
		verifyNoMoreInteractions(msg, frame, session, basic);
	}

	@Test
	public void fromBroker_ioe() throws IOException, EncodeException {
		final Message msg = mock(Message.class);
		when(msg.sessionId()).thenReturn("sessionId");
		final Frame frame = mock(Frame.class);
		when(msg.frame()).thenReturn(frame);
		when(frame.getCommand()).thenReturn(Command.MESSAGE);
		final Session session = Mockito.mock(Session.class);
		getSessionMap().put("sessionId", session);
		getPrincipalSessionMap().put(NULL_PRINCIPLE, new HashSet<>(singleton(session)));
		final Basic basic = mock(Basic.class);
		when(session.getBasicRemote()).thenReturn(basic);
		final IOException ioe = new IOException();
		doThrow(ioe).when(basic).sendObject(frame);

		this.registry.fromBroker(msg);

		verify(msg).sessionId();
		verify(msg).frame();
		verify(frame, times(3)).getCommand();
		verify(this.log).debug("Sending message to client. [sessionId={},command={}]", "sessionId", Command.MESSAGE);
		verify(session).getBasicRemote();
		verify(basic).sendObject(frame);
		verify(this.log).warn("Unable to send message! [sessionid={},command={}]", "sessionId", Command.MESSAGE, ioe);
		verifyNoMoreInteractions(msg, frame, session, basic);
	}

	@After
	public void after() {
		verifyNoMoreInteractions(this.log);
	}

	private ConcurrentMap<String, Session> getSessionMap() {
		return ReflectionUtil.get(this.registry, "sessionMap");
	}

	private ConcurrentMap<Principal, Set<Session>> getPrincipalSessionMap() {
		return ReflectionUtil.get(this.registry, "principalSessionMap");
	}
}
