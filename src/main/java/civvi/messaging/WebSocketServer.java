package civvi.messaging;

import static civvi.messaging.annotation.Qualifiers.fromClient;
import static civvi.messaging.annotation.Qualifiers.onClose;
import static civvi.messaging.annotation.Qualifiers.onError;
import static civvi.messaging.annotation.Qualifiers.onOpen;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
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

import civvi.messaging.annotation.FromBroker;
import civvi.messaging.annotation.FromClient;
import civvi.messaging.event.Message;
import civvi.stomp.Frame;
import civvi.stomp.FrameEncoding;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [15 Jul 2016]
 */
@ServerEndpoint(value = "/websocket", subprotocols = "STOMP", encoders = FrameEncoding.class, decoders = FrameEncoding.class)
public class WebSocketServer {
	@Inject
	private Logger log;
	@Inject
	private WebSocketSessionRegistry registry;
	@Inject @FromClient
	private Event<Message> messageEvent;
	@Inject
	private Event<Session> sessionEvent;

	@PostConstruct
	public void init() {
		log.info("WebSocketServer#init()");
	}

	@OnOpen
	public void open(Session session, EndpointConfig config) {
		this.log.info("WebSocket connection opened. [id={},principle={}]", session.getId(), session.getUserPrincipal());
		this.registry.register(session);
		this.sessionEvent.select(onOpen()).fire(session);
	}

	@OnMessage
	public void message(Session session, Frame msg) {
		this.log.info("WebSocket message. [id={},principle={}]", session.getId(), session.getUserPrincipal());
		this.messageEvent.select(fromClient()).fire(new Message(session.getId(), msg));
	}

	@OnClose
	public void close(Session session, CloseReason reason) {
		this.log.info("WebSocket closed. [id={},principle={},reason={}]", session.getId(), session.getUserPrincipal(), reason.getReasonPhrase());
		this.registry.unregister(session);
		this.sessionEvent.select(onClose()).fire(session);
	}

	@OnError
	public void error(Session session, Throwable t) {
		this.log.warn("WebSocket error. [id={},principle={}]", session.getId(), session.getUserPrincipal(), t);
		this.sessionEvent.select(fromClient(), onError()).fire(session);
	}

	/**
	 * 
	 * @param msg
	 */
	public void message(@Observes @FromBroker Message msg) {
		this.log.info("Sending message to client. [sessionId={},command={}]", msg.sessionId, msg.frame.getCommand());
		try {
			registry.getSession(msg.sessionId).getBasicRemote().sendObject(msg.frame);
		} catch (EncodeException | IOException e) {
			throw new RuntimeException(e);
		}
	}
}
