package cito.server.ws;

import cito.stomp.Frame;
import cito.stomp.FrameEncoding;
import cito.stomp.server.AbstractServer;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [15 Jul 2016]
 */
@ServerEndpoint(
		value = "/websocket",
		subprotocols = { "v11.stomp", "v12.stomp" },
		encoders = FrameEncoding.class,
		decoders = FrameEncoding.class,
		configurator = WebSocketConfigurator.class
)
public class WebSocketServer extends AbstractServer { }
