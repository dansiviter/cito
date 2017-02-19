package cito.sockjs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [4 Jan 2017]
 */
public class Context {
	private final Map<String, ServletSession> sessions = new ConcurrentHashMap<>();

	private final Config config;

	private boolean webSocketSupported;

	public Context(Config config) {
		this.config = config;
	}

	public Config getConfig() {
		return config;
	}

	public ServletSession register(ServletSession session) {
		return this.sessions.putIfAbsent(session.getId(), session);
	}

	/**
	 * 
	 * @param sessionId
	 * @return
	 */
	public ServletSession getSession(String sessionId) {
		return this.sessions.get(sessionId);
	}

	/**
	 * 
	 * @param sessionId
	 */
	public void unregister(String sessionId) {
		this.sessions.remove(sessionId);
	}

	/**
	 * @return the webSocketSupported
	 */
	public boolean isWebSocketSupported() {
		return webSocketSupported;
	}

	/**
	 * 
	 * @param supported
	 */
	void setWebSocketSupported(boolean supported) {
		this.webSocketSupported = supported;
	}
}
