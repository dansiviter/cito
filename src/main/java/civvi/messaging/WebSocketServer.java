package civvi.messaging;

import java.io.IOException;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;

import civvi.stomp.Frame;
import civvi.stomp.FrameEncoding;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [15 Jul 2016]
 */
@ServerEndpoint(value = "/websocket", subprotocols = "STOMP", encoders = FrameEncoding.class, decoders = FrameEncoding.class)
@RequestScoped
public class WebSocketServer {
	@Inject
	private Logger log;
	@Inject
	private WebSocketSessionRegistry registry;

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		this.log.info("WebSocket connection opened. [id={},principle={}]", session.getId(), session.getUserPrincipal());
		this.registry.register(session.getUserPrincipal(), session);
	}

	@OnMessage
	public void onMessage(Session session, Frame msg) {
		this.log.debug("WebSocket message. [id={},principle={}]", session.getId(), session.getUserPrincipal());
		switch (msg.getCommand()) {
		case CONNECT:
			doConnect(session, msg);
			break;

		default:
			throw new IllegalArgumentException("Unexpected command " + msg.getCommand() + "!");
		}


		System.out.println("onMessage: " + session.getId() + " - " + msg);
	}

	private void doConnect(Session session, Frame msg) {
		try {
			session.getBasicRemote().sendObject(Frame.connnected("1.1", null, null, null));
		} catch (IOException | EncodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@OnClose
	public void onClose(Session session, CloseReason reason) {
		this.log.info("WebSocket closed. [id={},principle={},reason={}]", session.getId(), session.getUserPrincipal(), reason.getReasonPhrase());
		this.registry.register(session.getUserPrincipal(), session);

	}

	@OnError
	public void onError(Session session, Throwable t) {
		this.log.warn("WebSocket error. [id={},principle={}]", session.getId(), session.getUserPrincipal(), t);
	}
}
