package cito.stomp.jms;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.jms.JMSException;
import javax.websocket.Session;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.api.provider.DependentProvider;
import org.apache.logging.log4j.Logger;

import cito.stomp.server.SecurityContext;
import cito.stomp.server.annotation.FromBroker;
import cito.stomp.server.annotation.FromClient;
import cito.stomp.server.annotation.OnClose;
import cito.stomp.server.event.MessageEvent;
import cito.stomp.server.security.SecurityRegistry;

/**
 * STOMP broker relay to JMS.
 * 
 * @author Daniel Siviter
 * @since v1.0 [19 Jul 2016]
 */
@ApplicationScoped
public class Relay {
	private final Map<String, DependentProvider<? extends AbstractConnection>> sessions = new ConcurrentHashMap<>();

	@Inject
	private Logger log;
	@Inject
	private BeanManager manager;
	@Inject @FromBroker
	private Event<MessageEvent> messageEvent;
	@Inject
	private ErrorHandler errorHandler;
	@Inject
	private SecurityRegistry securityRegistry;
	@Inject
	private Provider<SecurityContext> securityCtx;

	@PostConstruct
	public void init() {
		final DependentProvider<SystemConnection> conn = BeanProvider.getDependent(this.manager, SystemConnection.class);
		conn.get().connect();
		this.sessions.put(conn.get().getSessionId(), conn);
	}

	/**
	 * 
	 * @param msg
	 */
	public void message(@Observes @FromClient MessageEvent msg) {
		final String sessionId = msg.sessionId != null ? msg.sessionId : SystemConnection.SESSION_ID;

//		if (!securityRegistry.isPermitted(msg.frame, this.securityCtx.get())) {
//			throw new RuntimeException();
//		}

		try {
			DependentProvider<? extends AbstractConnection> conn = this.sessions.get(sessionId);
			if (msg.frame.getCommand() != null) {
				switch (msg.frame.getCommand()) {
				case CONNECT:
				case STOMP:
					this.log.info("CONNECT/STOMP recieved. Opening connection to broker. [sessionId={}]", sessionId);
					if (conn != null) {
						throw new IllegalStateException("Connection already exists! [sessionId=" + sessionId + "]");
					}
					conn = BeanProvider.getDependent(this.manager, Connection.class);
					((Connection) conn.get()).connect(msg);
					this.sessions.put(sessionId, conn);
					return;
				case DISCONNECT:
					this.log.info("DISCONNECT recieved. Closing connection to broker. [sessionId={}]", sessionId);
					conn.get().on(msg);
					close(sessionId);
					return;
				default:
					break;
				}
			}
			conn.get().on(msg);
		} catch (JMSException | RuntimeException e) {
			this.log.error("Unable to process message! [sessionId={},command={}]", sessionId, msg.frame.getCommand(), e);
			this.errorHandler.onError(this, sessionId, msg.frame, e);
		}
	}

	/**
	 * 
	 * @param sessionId
	 */
	public void close(String sessionId) {
		this.sessions.computeIfPresent(sessionId, (k, v) -> {
			log.info("Destroying JMS connection. [{}]", k);
			v.destroy();
			return null;
		});
	}

	/**
	 * 
	 * @param msg
	 */
	public void close(@Observes @OnClose Session session) {
		close(session.getId());
	}

	/**
	 * 
	 * @param msg
	 */
	public void send(MessageEvent msg) {
		this.messageEvent.fire(msg);
	}

	@PreDestroy
	public void destroy() {
		this.sessions.remove(SystemConnection.SESSION_ID).destroy();
	}
}