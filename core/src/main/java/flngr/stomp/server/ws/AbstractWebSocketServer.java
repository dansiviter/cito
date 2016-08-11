package flngr.stomp.server.ws;

import static flngr.stomp.server.annotation.Qualifiers.fromClient;
import static flngr.stomp.server.annotation.Qualifiers.onClose;
import static flngr.stomp.server.annotation.Qualifiers.onOpen;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import org.slf4j.Logger;

import flngr.stomp.Frame;
import flngr.stomp.server.SessionRegistry;
import flngr.stomp.server.annotation.FromClient;
import flngr.stomp.server.event.Message;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [15 Jul 2016]
 */
public abstract class AbstractWebSocketServer {
	@Inject
	private Logger log;
	@Inject
	private SessionRegistry registry;
	@Inject @FromClient
	private Event<Message> messageEvent;
	@Inject
	private Event<Session> sessionEvent;

	/**
	 * 
	 * @param session
	 * @param config
	 */
	protected void open(Session session, EndpointConfig config) {
		this.log.info("WebSocket connection opened. [id={},httpSessionId={},principle={}]",
				session.getId(),
				session.getRequestParameterMap().get("httpSessionId"),
				session.getUserPrincipal());
		this.registry.register(session);
		this.sessionEvent.select(onOpen()).fire(session);
	}

	/**
	 * 
	 * @param session
	 * @param frame
	 */
	protected void message(Session session, Frame frame) {
		this.log.info("WebSocket message. [id={},principle={},command={}]", session.getId(), session.getUserPrincipal(), frame.getCommand());
		this.messageEvent.select(fromClient()).fire(new Message(session.getId(), frame));
	}

	/**
	 * 
	 * @param session
	 * @param reason
	 */
	protected void close(Session session, CloseReason reason) {
		this.log.info("WebSocket closed. [id={},principle={},code={},reason={}]", session.getId(), session.getUserPrincipal(), reason.getCloseCode(), reason.getReasonPhrase());
		this.registry.unregister(session);
		this.sessionEvent.select(onClose()).fire(session);
	}

	/**
	 * 
	 * @param session
	 * @param t
	 */
	protected void error(Session session, Throwable t) {
		this.log.warn("WebSocket error. [id={},principle={}]", session.getId(), session.getUserPrincipal(), t);
	}
}
