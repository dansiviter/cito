package cito.stomp.server.ws;

import java.security.Principal;

import javax.websocket.server.HandshakeRequest;

import cito.stomp.server.SecurityContext;

/**
 * Defines {@link javax.ws.rs.core.SecurityContext} for WebSocket connections.
 * 
 * @author Daniel Siviter
 * @since v1.0 [17 Aug 2016]
 */
public class WebSocketSecurityContext implements SecurityContext {
	private static final long serialVersionUID = 2495929824931036726L;

	private final HandshakeRequest req;

	public WebSocketSecurityContext(HandshakeRequest req) {
		this.req = req;
	}

	@Override
	public Principal getUserPrincipal() {
		return this.req.getUserPrincipal();
	}

	@Override
	public boolean isUserInRole(String role) {
		return this.req.isUserInRole(role);
	}
}
