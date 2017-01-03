package cito.sockjs;

import java.util.List;

import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import cito.stomp.server.AbstractServer;

/**
 * Concrete definition of a SockJS WebSocket endpoint.
 * 
 * @author Daniel Siviter
 * @since v1.0 [15 Jul 2016]
 */
public class WebSocketServer extends AbstractServer {
	@Override
	public void open(Session session, EndpointConfig config) {
		super.open(session, config);
		final List<String> sockJsSession = session.getRequestParameterMap().get("session");
		if (sockJsSession != null && !sockJsSession.isEmpty()) {
			this.log.info("SockJS session mapping. [sockJs={},webSocket={}]", sockJsSession.get(0), session.getId());
		}
	}
}
