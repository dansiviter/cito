package cito.server.sockjs.ws;

import java.util.Map;

import javax.websocket.EndpointConfig;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import cito.server.ws.WebSocketConfigurator;
import cito.stomp.FrameEncoding;
import cito.stomp.server.AbstractServer;

/**
 * Defines a SockJS WebSocket endpoint.
 * 
 * @author Daniel Siviter
 * @since v1.0 [15 Jul 2016]
 */
@ServerEndpoint(
		value = "/{server}/{session}/websocket",
		subprotocols = { "v10.stomp", "v11.stomp", "v12.stomp" },
		encoders = FrameEncoding.class,
		decoders = FrameEncoding.class,
		configurator = WebSocketConfigurator.class
)
public class WebSocketServer extends AbstractServer {
	@OnOpen
	public void open(
			Session session,
			EndpointConfig config,
			@PathParam("server") String sockJsServer,
			@PathParam("session") String sockJsSession)
	{
		final Map<String, Object> userProperties = session.getUserProperties();
		userProperties.put("sockJsServer", sockJsServer);
		userProperties.put("sockJsSession", sockJsSession);
		super.open(session, config);
	}
}
