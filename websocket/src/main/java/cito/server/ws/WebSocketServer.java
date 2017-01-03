package cito.server.ws;

import javax.websocket.server.ServerEndpoint;

import cito.stomp.server.AbstractServer;
import cito.stomp.server.ws.FrameEncoding;
import cito.stomp.server.ws.WebSocketConfigurator;

/**
 * Defines a basic WebSocket endpoint.
 * 
 * @author Daniel Siviter
 * @since v1.0 [15 Jul 2016]
 */
@ServerEndpoint(
		value = "/websocket",
		subprotocols = { "v10.stomp", "v11.stomp", "v12.stomp" },
		encoders = FrameEncoding.class,
		decoders = FrameEncoding.class,
		configurator = WebSocketConfigurator.class
)
public class WebSocketServer extends AbstractServer { }
