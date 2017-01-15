package cito.sockjs;

import static java.util.Objects.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.Endpoint;
import javax.websocket.Session;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [4 Jan 2017]
 */
public class Context {
	private final Map<String, EndpointHolder> sessions = new ConcurrentHashMap<>();

	private final Initialiser initialiser;

	public Context(Initialiser initialiser) {
		this.initialiser = initialiser;
	}

	public Initialiser getInitialiser() {
		return initialiser;
	}

	public void register(Session session, Endpoint endpoint) {
		this.sessions.put(session.getId(), new EndpointHolder(
				requireNonNull(session),
				requireNonNull(endpoint)));
	}

	/**
	 * 
	 * @param sessionId
	 * @return
	 */
	public Session getSession(String sessionId) {
		return this.sessions.get(sessionId).session;
	}

	/**
	 * 
	 * @param sessionId
	 * @return
	 */
	public Endpoint getEndpoint(String sessionId) {
		return this.sessions.get(sessionId).endpoint;
	}

	/**
	 * 
	 * @param sessionId
	 */
	public void unregister(String sessionId) {
		this.sessions.remove(sessionId);
	}


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [4 Jan 2017]
	 */
	private static class EndpointHolder {
		private final Session session;
		private final Endpoint endpoint;

		public EndpointHolder(Session session, Endpoint endpoint) {
			this.session = session;
			this.endpoint = endpoint;
		}
	}
}
