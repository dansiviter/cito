package flngr.stomp.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.http.HttpSession;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [9 Aug 2016]
 */
@ApplicationScoped
public class HttpSessionRegistry {
	private final Map<String, HttpSession> sessions = new ConcurrentHashMap<>();

	/**
	 * 
	 * @param session
	 */
	public void register(HttpSession session) {
		final HttpSession existing = this.sessions.computeIfAbsent(session.getId(), (v) -> { return session; });
		if (existing != null)
			throw new IllegalArgumentException("Session already registered! [" + session.getId() + "]");
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public HttpSession get(String id) {
		return this.sessions.get(id);
	}

	/**
	 * 
	 * @param session
	 */
	public void unregister(HttpSession session) {
		final HttpSession existing = this.sessions.computeIfPresent(session.getId(), (k, v) -> { return null; });
		if (existing == null)
			throw new IllegalArgumentException("Session not registered! [" + session.getId() + "]");
	}
}
