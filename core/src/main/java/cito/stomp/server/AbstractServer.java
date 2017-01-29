package cito.stomp.server;

import static cito.stomp.server.annotation.Qualifiers.fromClient;

import javax.enterprise.event.Event;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cito.QuietClosable;
import cito.stomp.Frame;
import cito.stomp.server.annotation.FromClient;
import cito.stomp.server.annotation.Qualifiers;
import cito.stomp.server.event.ClientMessageEventProducer;
import cito.stomp.server.event.MessageEvent;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [15 Jul 2016]
 */
public abstract class AbstractServer extends Endpoint {
	protected final Logger log;

	@Inject
	private BeanManager beanManager;
	@Inject
	private SessionRegistry registry;
	@Inject @FromClient
	private Event<MessageEvent> messageEvent;
	@Inject
	private Event<Session> sessionEvent;
	@Inject
	private Event<Throwable> errorEvent;

	/**
	 * 
	 */
	public AbstractServer() {
		this.log = LoggerFactory.getLogger(getClass());
	}

	@Override
	public void onOpen(Session session, EndpointConfig config) {
		final String httpSessionId = session.getRequestParameterMap().get("httpSessionId").get(0);
		this.log.info("WebSocket connection opened. [id={},httpSessionId={},principle={}]",
				session.getId(),
				httpSessionId,
				session.getUserPrincipal());
		// remove instances associated with 'httpSessionId' to avoid bloating
		session.getUserProperties().get(httpSessionId);
		final SecurityContext securityCtx = (SecurityContext) config.getUserProperties().remove(httpSessionId);
		session.getUserProperties().put(SecurityContext.class.getSimpleName(), securityCtx);
		try (QuietClosable c = Extension.activateScope(this.beanManager, session)) {
			this.registry.register(session);
			this.sessionEvent.select(Qualifiers.onOpen()).fire(session);
		}
	}

	/**
	 * 
	 * @param session
	 * @param frame
	 */
	public void message(Session session, Frame frame) {
		this.log.debug("Received message from client. [id={},principle={},command={}]", session.getId(), session.getUserPrincipal(), frame.getCommand());
		try (QuietClosable c = Extension.activateScope(this.beanManager, session)) {
			final MessageEvent event = new MessageEvent(session.getId(), frame);
			try (QuietClosable closable = ClientMessageEventProducer.set(event)) {
				this.messageEvent.select(fromClient()).fire(event);
			}
		}
	}

	@Override
	public void onError(Session session, Throwable t) {
		this.log.warn("WebSocket error. [id={},principle={}]", session.getId(), session.getUserPrincipal(), t);
		try (QuietClosable c = Extension.activateScope(this.beanManager, session)) {
			this.registry.unregister(session);
			this.errorEvent.select(Qualifiers.onError()).fire(t);
		}
	}

	@Override
	public void onClose(Session session, CloseReason reason) {
		this.log.info("WebSocket connection closed. [id={},principle={},code={},reason={}]", session.getId(), session.getUserPrincipal(), reason.getCloseCode(), reason.getReasonPhrase());
		try (QuietClosable c = Extension.activateScope(this.beanManager, session)) {
			this.registry.unregister(session);
			this.sessionEvent.select(Qualifiers.onClose()).fire(session);
		}
		Extension.disposeScope(this.beanManager, session);
	}
}
