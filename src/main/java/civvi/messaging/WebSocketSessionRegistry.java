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
	private final ConcurrentMap<Principal, Collection<Session>> sessionMap = new ConcurrentHashMap<>();

	/**
	 * 
	 * @param principal
	 * @param session
	 */
	public void register(Principal principal, Session session) {
		if (principal == null)
			principal = NULL_PRINCIPLE;
		this.sessionMap.compute(principal, (k, v) -> { v = v != null ? v : new HashSet<>(); v.add(session); return v; });
	}

	/**
	 * 
	 * @param principal
	 * @param session
	 */
	public void unregister(Principal principal, Session session) {
		if (principal == null)
			principal = NULL_PRINCIPLE;
		this.sessionMap.computeIfPresent(principal, (k, v) -> { v.remove(session); return v.isEmpty() ? null : v; });
	}

	/**
	 * 
	 * @param principal
	 * @return
	 */
	public Collection<Session> getSessions(Principal principal) {
		return Collections.unmodifiableCollection(this.sessionMap.get(principal));
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
