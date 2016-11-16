package cito.stomp.server;

import static cito.stomp.server.annotation.Qualifiers.fromClient;
import static cito.stomp.server.annotation.Qualifiers.onClose;
import static cito.stomp.server.annotation.Qualifiers.onOpen;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import org.apache.logging.log4j.Logger;

import cito.QuietClosable;
import cito.stomp.Frame;
import cito.stomp.server.annotation.FromClient;
import cito.stomp.server.event.MessageEvent;
import cito.stomp.server.event.SerialisingMessageEvent;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [15 Jul 2016]
 */
public abstract class AbstractServer {
	@Inject
	private Logger log;
	@Inject
	private BeanManager beanManager;
	@Inject
	private SessionRegistry registry;
	@Inject @FromClient
	private Event<MessageEvent> messageEvent;
	@Inject
	private Event<Session> sessionEvent;
	@Inject
	private Instance<SerialisingMessageEvent> messageEventInstance;

	/**
	 * 
	 * @param session
	 * @param config
	 */
	protected void open(Session session, EndpointConfig config) {
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
			this.sessionEvent.select(onOpen()).fire(session);
		}
	}

	/**
	 * 
	 * @param session
	 * @param frame
	 */
	protected void message(Session session, Frame frame) {
		this.log.info("WebSocket message. [id={},principle={},command={}]", session.getId(), session.getUserPrincipal(), frame.getCommand());
		try (QuietClosable c = Extension.activateScope(this.beanManager, session)) {
			final SerialisingMessageEvent event = this.messageEventInstance.get();
			event.init(session.getId(), frame);
			this.messageEvent.select(fromClient()).fire(event);
			this.messageEventInstance.destroy(event);
		}
	}

	/**
	 * 
	 * @param session
	 * @param reason
	 */
	protected void close(Session session, CloseReason reason) {
		this.log.info("WebSocket closed. [id={},principle={},code={},reason={}]", session.getId(), session.getUserPrincipal(), reason.getCloseCode(), reason.getReasonPhrase());
		try (QuietClosable c = Extension.activateScope(this.beanManager, session)) {
			this.registry.unregister(session);
			this.sessionEvent.select(onClose()).fire(session);
		}
		Extension.disposeScope(this.beanManager, session);
	}

	/**
	 * 
	 * @param session
	 * @param t
	 */
	protected void error(Session session, Throwable t) {
		this.log.warn("WebSocket error. [id={},principle={}]", session.getId(), session.getUserPrincipal(), t);
		try (QuietClosable c = Extension.activateScope(this.beanManager, session)) {
//			Extension.getWebSocketContext(this.beanManager).destroyAllActive();
		} catch (RuntimeException e) {
			log.error("Exception while trying to handle error! Error will be re-thrown, but original is logged here.", t);
			throw e;
		}
	}
}
