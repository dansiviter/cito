package cito.server.ws;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

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
public class WebSocketServer extends AbstractServer {
	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		open(session, config);
	}

	@OnMessage
	public void onMessage(Session session, Frame frame) {
		message(session, frame);
	}

	@OnClose
	public void onClose(Session session, CloseReason reason) {
		close(session, reason);
	}

	@OnError
	public void onError(Session session, Throwable t) {
		error(session, t);
	}
}
