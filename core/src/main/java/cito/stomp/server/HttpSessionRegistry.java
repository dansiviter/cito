package cito.stomp.server;

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
		this.sessions.computeIfAbsent(session.getId(), (v) -> { return session; });
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
		this.sessions.computeIfPresent(session.getId(), (k, v) -> { return null; });
	}
}
