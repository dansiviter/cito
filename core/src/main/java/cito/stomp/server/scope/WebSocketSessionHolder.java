package cito.stomp.server.scope;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cito.stomp.server.annotation.WebSocketScope;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [17 Aug 2016]
 */
@ApplicationScoped
public class WebSocketSessionHolder {
	private static final Logger LOG = LoggerFactory.getLogger(WebSocketSessionHolder.class);

	private final ThreadLocal<Session> session = new ThreadLocal<>();

	/**
	 * 
	 * @param session
	 */
	public void set(Session session) {
		LOG.debug("Setting session. [sessionId={}]", session.getId());
		if (this.session.get() != null) {
			throw new IllegalStateException("Session already set! [expected=" + this.session.get().getId() + ",current=" + session.getId() + "]");
		}
		this.session.set(session);
	}

	/**
	 * 
	 */
	public void remove() {
		final Session session = this.session.get();
		if (session == null) {
			throw new IllegalArgumentException("Session not set!");
		}
		LOG.debug("Removing session. [sessionId={}]", session.getId());
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
