package cito.server.ws;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

import cito.stomp.server.SecurityContext;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Sep 2016]
 */
public class WebSocketConfigurator extends Configurator {
	@Override
	public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
		final HttpSession httpSession = (HttpSession) request.getHttpSession();
		final SecurityContext securityCtx = new WebSocketSecurityContext(request);
		sec.getUserProperties().put(httpSession.getId(), securityCtx);
	}
}
