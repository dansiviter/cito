package cito.sockjs;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [3 Jan 2017]
 */
public class SockJsSession extends SessionAdapter {
	private final String sessionId;

	public SockJsSession(String sessionId) {
		this.sessionId = sessionId;
	}

	@Override
	public String getId() {
		return this.sessionId;
	}
}
