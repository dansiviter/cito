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

import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.websocket.EncodeException;
import javax.websocket.Session;

import org.slf4j.Logger;

import cito.annotation.FromBroker;
import cito.event.Message;
import cito.stomp.Frame;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [15 Jul 2016]
 */
@ApplicationScoped
public class SessionRegistry {
	@Inject
	private Logger log;

	static final Principal NULL_PRINCIPLE = new NullPrinciple();
	private final ConcurrentMap<String, Session> sessionMap = new ConcurrentHashMap<>();
	private final ConcurrentMap<Principal, Set<Session>> principalSessionMap = new ConcurrentHashMap<>();

	/**
	 * 
	 * @param session
	 */
	public void register(Session session) {
		final Session oldSession = this.sessionMap.put(session.getId(), session);
		if (oldSession != null)
			throw new IllegalArgumentException("Session already registered! [" + session.getId() + "]");
		Principal principal = session.getUserPrincipal();
		if (principal == null)
			principal = NULL_PRINCIPLE;
		this.principalSessionMap.compute(principal, (k, v) -> { v = v != null ? v : new HashSet<>(); v.add(session); return v; });
	}

	/**
	 * 
	 * @param session
	 */
	public void unregister(Session session) {
		final String sessionId = session.getId();
		final Session oldSession = this.sessionMap.remove(sessionId);
		if (oldSession == null)
			throw new IllegalArgumentException("Session not registered! [" + sessionId + "]");
		Principal principal = session.getUserPrincipal();
		if (principal == null)
			principal = NULL_PRINCIPLE;
		this.principalSessionMap.computeIfPresent(principal, (k, v) -> { v.remove(session); return v.isEmpty() ? null : v; });
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public Optional<Session> getSession(String id) {
		return Optional.ofNullable(this.sessionMap.get(id));
	}

	/**
	 * 
	 * @param principal
	 * @return
	 */
	public Set<Session> getSessions(Principal principal) {
		final Set<Session> sessions = this.principalSessionMap.get(principal);
		return sessions != null ? Collections.unmodifiableSet(sessions) : Collections.emptySet();
	}

	/**
	 * 
	 * @param msg
	 */
	public void fromBroker(@Observes @FromBroker Message msg) {
		final String sessionId = msg.sessionId();
		final Frame frame = msg.frame();
		this.log.debug("Sending message to client. [sessionId={},command={}]",
				sessionId, frame.getCommand() != null ? frame.getCommand() : "HEARTBEAT");

			final Session session = getSession(sessionId).orElseThrow(
					() -> new IllegalStateException("Session does not exist! [" + sessionId + "]"));
		try {
			session.getBasicRemote().sendObject(frame);
		} catch (IOException | EncodeException e) {
			this.log.warn("Unable to send message! [sessionid={},command={}]", sessionId, frame.getCommand(), e);
		}
	}


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [15 Jul 2016]
	 */
	private static class NullPrinciple implements Principal {
		@Override
		public String getName() {
			return null;
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public boolean equals(Object obj) {
			return getClass() == obj.getClass();
		}
	}
}
