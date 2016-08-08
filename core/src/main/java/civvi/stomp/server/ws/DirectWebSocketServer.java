package civvi.stomp.server.ws;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import civvi.stomp.Frame;
import civvi.stomp.FrameEncoding;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Jul 2016]
 */
@ServerEndpoint(
		value = "/websocket",
		subprotocols = { "v11.stomp", "v12.stomp" },
		encoders = FrameEncoding.class,
		decoders = FrameEncoding.class
)
public class DirectWebSocketServer extends AbstractWebSocketServer {
	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		super.open(session, config);
	}

	@OnMessage
	public void onMessage(Session session, Frame frame) {
		super.message(session, frame);
	}

	@OnClose
	public void onClose(Session session, CloseReason reason) {
		super.close(session, reason);
	}

	@OnError
	public void onError(Session session, Throwable t) {
		super.error(session, t);
	}
}
