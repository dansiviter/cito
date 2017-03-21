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
package cito.server;

import static cito.annotation.Qualifiers.fromClient;

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
import cito.annotation.FromClient;
import cito.annotation.Qualifiers;
import cito.event.ClientMessageEventProducer;
import cito.event.MessageEvent;
import cito.stomp.Frame;
import cito.stomp.jms.Relay;

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
	@Inject
	private Relay relay;
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
				this.relay.fromClient(event); // due to no @Observe @Priority we need to ensure the relay gets this first
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
