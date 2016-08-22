package cito.stomp.server.scope;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.websocket.Session;

import cito.stomp.server.annotation.WebSocketScope;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [17 Aug 2016]
 */
@ApplicationScoped
public class WebSocketSessionHolder {
	private final ThreadLocal<Session> session = new ThreadLocal<>();

	/**
	 * 
	 * @param session
	 */
	public void set(Session session) {
		if (this.session.get() != null) {
			throw new IllegalStateException("Session already set!");
		}
		this.session.set(session);
	}

	/**
	 * 
	 */
	public void remove() {
		if (this.session.get() == null) {
			throw new IllegalArgumentException("Session not set!");
		}
		this.session.remove();
	}

	/**
	 * 
	 * @return
	 */
	@Produces @WebSocketScope
	public Session get() {
		return this.session.get();
	}
}
