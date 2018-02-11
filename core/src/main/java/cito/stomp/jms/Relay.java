/*
 * Copyright 2016-2017 Daniel Siviter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cito.stomp.jms;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.ObservesAsync;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.jms.JMSException;
import javax.security.auth.login.LoginException;
import javax.websocket.Session;

import org.slf4j.Logger;

import cito.annotation.FromServer;
import cito.annotation.OnClose;
import cito.event.Message;
import cito.server.SecurityContext;
import cito.server.SessionRegistry;
import cito.server.security.SecurityRegistry;

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
	@Inject
	private SessionRegistry sessionRegistry;
	@Inject
	private ErrorHandler errorHandler;
	@Inject
	private SecurityRegistry securityRegistry;
	@Inject
	private Instance<Connection> connectionInstance;
	@Inject
	private Provider<SecurityContext> securityCtx;
	@Inject
	private SystemConnection systemConn;

	/**
	 * Message from the client.
	 * 
	 * @param msg
	 */
	public void fromClient(@Nonnull Message msg) {
		final String sessionId = msg.sessionId();
		this.log.debug("Message from client. [sessionId={},command={}]", sessionId, msg.frame().command());

		final boolean permitted = this.securityRegistry.isPermitted(msg.frame(), this.securityCtx.get());
		if (!permitted) {
			this.errorHandler.onError(this, sessionId, msg.frame(), "Not permitted!", null);
			return;
		}
		on(msg);
	}

	/**
	 * Message from the server layer.
	 * 
	 * @param evt
	 */
	public void fromServer(@Observes @ObservesAsync @FromServer Message evt) {
		if (this.log.isDebugEnabled()) {
			this.log.debug("Message event from server. [sessionId={},command={}]", evt.sessionId(),
					evt.frame().command());
		}
		on(evt);
	}

	/**
	 * 
	 * @param evt
	 */
	private void on(Message evt) {
		final String sessionId = evt.sessionId() != null ? evt.sessionId() : SystemConnection.SESSION_ID;
		try {
			AbstractConnection conn = evt.sessionId() != null ? this.connections.get(sessionId) : this.systemConn;

			if (evt.frame().command() != null) {
				switch (evt.frame().command()) {
				case CONNECT:
				case STOMP:
					this.log.info("CONNECT/STOMP recieved. Opening connection to broker. [sessionId={}]", sessionId);
					if (conn != null) {
						throw new IllegalStateException("Connection already exists! [sessionId=" + sessionId + "]");
					}
					final Connection newConn = this.connectionInstance.get();
					this.connections.put(sessionId, newConn);
					newConn.connect(evt);
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
				this.log.error("Session not found! Was there a previous error? [{}]", sessionId);
				return;
			}
			conn.on(evt);
		} catch (JMSException | RuntimeException | LoginException e) {
			this.errorHandler.onError(this, sessionId, evt.frame(), null, e);
		}
	}

	/**
	 * 
	 * @param sessionId
	 */
	public void close(@Nonnull String sessionId) {
		this.connections.computeIfPresent(sessionId, (k, c) -> {
			log.info("Destroying JMS connection. [{}]", k);
			this.connectionInstance.destroy(c);
			return null;
		});
		// FIXME we need to decouple this from the websocket sessions
		this.sessionRegistry.getSession(sessionId).ifPresent(s -> {
			try {
				if (s.isOpen())
					s.close();
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
}