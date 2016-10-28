package cito.stomp.server.scope;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.websocket.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cito.stomp.server.annotation.WebSocketScope;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [17 Aug 2016]
 */
@ApplicationScoped
public class WebSocketSessionHolder {
	private static final Logger LOG = LogManager.getLogger(WebSocketSessionHolder.class);

	private final ThreadLocal<Session> session = new ThreadLocal<>();

	/**
	 * 
	 * @param session
	 */
	public void set(Session session) {
		LOG.debug("Setting session. [sessionId={}]", session.getId());
		if (this.session.get() != null) {
			throw new IllegalStateException("Session already set!");
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
		LOG.info("Returning session. [sessionId={}]", this.session.get().getId());
		return this.session.get();
	}
}
