package cito.stomp.server;

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

import org.apache.logging.log4j.Logger;

import cito.stomp.server.annotation.FromBroker;
import cito.stomp.server.annotation.FromServer;
import cito.stomp.server.event.MessageEvent;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [15 Jul 2016]
 */
@ApplicationScoped
public class SessionRegistry {
	@Inject
	private Logger log;

	private static final Principal NULL_PRINCIPLE = new NullPrinciple();
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
		final Session oldSession = this.sessionMap.remove(session.getId());
		if (oldSession == null)
			throw new IllegalArgumentException("Session not registered! [" + session.getId() + "]");
		Principal principal = session.getUserPrincipal();
		if (principal == null)
			principal = NULL_PRINCIPLE;
		this.principalSessionMap.computeIfPresent(principal, (k, v) -> { v.remove(session); return v.isEmpty() ? null : v; });
	}

	public Optional<Session> getSession(String id) {
		return Optional.ofNullable(this.sessionMap.get(id));
	}

	/**
	 * 
	 * @param principal
	 * @return
	 */
	public Set<Session> getSessions(Principal principal) {
		return Collections.unmodifiableSet(this.principalSessionMap.get(principal));
	}

	/**
	 * 
	 * @param msg
	 */
	public void fromBroker(@Observes @FromBroker MessageEvent msg) {
		this.log.debug("Sending message to client. [sessionId={},command={}]",
				msg.sessionId(), msg.frame().getCommand() != null ? msg.frame().getCommand() : "HEARTBEAT");

			final Session session = getSession(msg.sessionId()).orElseThrow(
					() -> new IllegalStateException("Session does not exist! [" + msg.sessionId() + "]"));
		try {
			session.getBasicRemote().sendObject(msg.frame());
		} catch (IOException | EncodeException e) {
			this.log.warn("Unable to send message! [sessionid={},command={}]", msg.sessionId(), msg.frame().getCommand(), e);
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
