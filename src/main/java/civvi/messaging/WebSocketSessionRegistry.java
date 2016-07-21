package civvi.messaging;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.Session;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [15 Jul 2016]
 */
@ApplicationScoped
public class WebSocketSessionRegistry {
	private static final Principal NULL_PRINCIPLE = new NullPrinciple();
	private final ConcurrentMap<String, Session> sessionMap = new ConcurrentHashMap<>();
	private final ConcurrentMap<Principal, Collection<Session>> principalSessionMap = new ConcurrentHashMap<>();

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
	
	public Session getSession(String id) {
		return this.sessionMap.get(id);
	}

	/**
	 * 
	 * @param principal
	 * @return
	 */
	public Collection<Session> getSessions(Principal principal) {
		return Collections.unmodifiableCollection(this.principalSessionMap.get(principal));
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
