package cito.stomp.jms;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.jms.JMSException;
import javax.websocket.Session;

import org.slf4j.Logger;

import cito.stomp.server.SecurityContext;
import cito.stomp.server.SessionRegistry;
import cito.stomp.server.annotation.FromBroker;
import cito.stomp.server.annotation.FromClient;
import cito.stomp.server.annotation.FromServer;
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
	private final Map<String, Connection> connections = new ConcurrentHashMap<>();

	@Inject
	private Logger log;
	@Inject @FromBroker
	private Event<MessageEvent> messageEvent;
	@Inject
	private SessionRegistry sessionRegistry;
	@Inject
	private ErrorHandler errorHandler;
	@Inject
	private SecurityRegistry securityRegistry;
	@Inject
	private Provider<SecurityContext> securityCtx;
	@Inject
	private Instance<Connection> connectionInstance;
	@Inject
	private SystemConnection systemConn;

	/**
	 * 
	 * @param msg
	 */
	public void clientMessage(@Observes @FromClient MessageEvent msg) {
		final String sessionId = msg.sessionId() != null ? msg.sessionId() : SystemConnection.SESSION_ID;

		final boolean permitted = this.securityRegistry.isPermitted(msg.frame(), this.securityCtx.get());
		if (!permitted) {
			this.errorHandler.onError(this, sessionId, msg.frame(), "Not permitted!", null);
			return;
		}
		on(msg);
	}

	/**
	 * 
	 * @param evt
	 */
	public void on(@Observes @FromServer MessageEvent evt) {
		final String sessionId = evt.sessionId() != null ? evt.sessionId() : SystemConnection.SESSION_ID;

		try {
			AbstractConnection conn = evt.sessionId() != null ? this.connections.get(sessionId) : this.systemConn;

			if (evt.frame().getCommand() != null) {
				switch (evt.frame().getCommand()) {
				case CONNECT:
				case STOMP:
					this.log.info("CONNECT/STOMP recieved. Opening connection to broker. [sessionId={}]", sessionId);
					if (conn != null) {
						throw new IllegalStateException("Connection already exists! [sessionId=" + sessionId + "]");
					}
					final Connection newConn = this.connectionInstance.get();
					newConn.connect(evt);
					this.connections.put(sessionId, newConn);
					return;
				case DISCONNECT:
					this.log.info("DISCONNECT recieved. Closing connection to broker. [sessionId={}]", sessionId);
					((Connection) conn).disconnect(evt);
					close(sessionId);
					return;
				default:
					break;
				}
			}

			if (conn == null) {
				throw new IllegalStateException("Session not found! [" + sessionId + "]");
			}
			conn.on(evt);
		} catch (JMSException | RuntimeException e) {
			this.errorHandler.onError(this, sessionId, evt.frame(), null, e);
		}
	}

	/**
	 * 
	 * @param sessionId
	 */
	public void close(String sessionId) {
		this.connections.computeIfPresent(sessionId, (k, c) -> {
			log.info("Destroying JMS connection. [{}]", k);
			this.connectionInstance.destroy(c);
			return null;
		});
		// FIXME we need to decouple this from the websocket sessions
		this.sessionRegistry.getSession(sessionId).ifPresent(s -> {
			try {
				if (s.isOpen()) s.close();
			} catch (IOException e) {
				this.log.warn("Unable to close session. [sessionId={}]", sessionId, e);
			}
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
}