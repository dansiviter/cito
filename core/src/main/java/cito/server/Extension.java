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

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProcessObserverMethod;
import javax.websocket.Session;

import org.apache.deltaspike.core.api.provider.BeanProvider;

import cito.QuietClosable;
import cito.annotation.OnAdded;
import cito.annotation.OnConnected;
import cito.annotation.OnDisconnect;
import cito.annotation.OnRemoved;
import cito.annotation.OnSend;
import cito.annotation.OnSubscribe;
import cito.annotation.OnUnsubscribe;
import cito.annotation.WebSocketScope;
import cito.event.DestinationEvent;
import cito.event.MessageEvent;
import cito.scope.WebSocketContext;
import cito.scope.WebSocketSessionHolder;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [12 Jul 2016]
 */
public class Extension implements javax.enterprise.inject.spi.Extension {
	private final Map<Class<? extends Annotation>, Set<ObserverMethod<MessageEvent>>> messageObservers = new ConcurrentHashMap<>();
	private final Map<Class<? extends Annotation>, Set<ObserverMethod<DestinationEvent>>> destinationObservers = new ConcurrentHashMap<>();

	private WebSocketContext webSocketContext;

	/**
	 * 
	 * @param cls
	 * @param method
	 */
	private <A extends Annotation> void registerMessageObserver(Class<A> cls, ObserverMethod<MessageEvent> method) {
		Set<ObserverMethod<MessageEvent>> annotations = this.messageObservers.get(cls);
		if (annotations == null) {
			annotations = new HashSet<>();
			this.messageObservers.put(cls, annotations);
		}
		annotations.add(method);
	}

	/**
	 * 
	 * @param e
	 * @param beanManager
	 */
	public void registerMessageEvent(@Observes ProcessObserverMethod<MessageEvent, ?> e, BeanManager beanManager) {
		final ObserverMethod<MessageEvent> method = e.getObserverMethod();
		for (Annotation a : method.getObservedQualifiers()) {
			if (a instanceof OnConnected)
				registerMessageObserver(OnConnected.class, method);
			if (a instanceof OnSend)
				registerMessageObserver(OnSend.class, method);
			if (a instanceof OnSubscribe)
				registerMessageObserver(OnSubscribe.class, method);
			if (a instanceof OnUnsubscribe)
				registerMessageObserver(OnUnsubscribe.class, method);
			if (a instanceof OnDisconnect)
				registerMessageObserver(OnDisconnect.class, method);
		}
	}

	/**
	 * 
	 * @param qualifier
	 * @return
	 */
	public Set<ObserverMethod<MessageEvent>> getMessageObservers(Class<? extends Annotation> qualifier) {
		final Set<ObserverMethod<MessageEvent>> observers = this.messageObservers.get(qualifier);
		return observers == null ? Collections.emptySet() : observers;
	}

	/**
	 * 
	 * @param cls
	 * @param method
	 */
	private <A extends Annotation> void registerDestinationObserver(Class<A> cls, ObserverMethod<DestinationEvent> method) {
		Set<ObserverMethod<DestinationEvent>> annotations = this.destinationObservers.get(cls);
		if (annotations == null) {
			annotations = new HashSet<>();
			this.destinationObservers.put(cls, annotations);
		}
		annotations.add(method);
	}

	/**
	 * 
	 * @param e
	 * @param beanManager
	 */
	public void registerDestinationEvent(@Observes ProcessObserverMethod<DestinationEvent, ?> e, BeanManager beanManager) {
		final ObserverMethod<DestinationEvent> method = e.getObserverMethod();
		for (Annotation a : method.getObservedQualifiers()) {
			if (a instanceof OnAdded)
				registerDestinationObserver(OnAdded.class, method);
			if (a instanceof OnRemoved)
				registerDestinationObserver(OnRemoved.class, method);
		}
	}

	/**
	 * 
	 * @param qualifier
	 * @return
	 */
	public Set<ObserverMethod<DestinationEvent>> getDestinationObservers(Class<? extends Annotation> qualifier) {
		final Set<ObserverMethod<DestinationEvent>> observers = this.destinationObservers.get(qualifier);
		return observers == null ? Collections.emptySet() : observers;
	}

	/**
	 * 
	 * @param event
	 */
	public void addScope(@Observes BeforeBeanDiscovery event) {
		event.addScope(WebSocketScope.class, true, false);
	}

	/**
	 * 
	 * @param event
	 * @param beanManager
	 */
	public void registerContexts(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
		event.addContext(this.webSocketContext = new WebSocketContext(beanManager));
	}

	/**
	 * We can only initialize our contexts in AfterDeploymentValidation because
	 * getBeans must not be invoked earlier than this phase to reduce randomness
	 * caused by Beans no being fully registered yet.
	 */
	public void initialiseContexts(@Observes AfterDeploymentValidation adv, BeanManager beanManager)
	{
		final WebSocketSessionHolder sessionHolder = BeanProvider.getContextualReference(beanManager, WebSocketSessionHolder.class, false);
		this.webSocketContext.init(sessionHolder);
	}


	// --- Static Methods ---

	/**
	 * @return the instance of {@code WebSocketContext}.
	 */
	public static WebSocketContext getWebSocketContext(BeanManager manager) {
		return manager.getExtension(Extension.class).webSocketContext;
	}

	/**
	 * Activates the {@link WebSocketScope} for the {@link Session}.
	 * 
	 * @param manager
	 * @param session
	 * @return a {@link QuietClosable} that can be used to pacify the scope.
	 */
	public static QuietClosable activateScope(BeanManager manager, Session session) {
		return getWebSocketContext(manager).activate(session);
	}

	/**
	 * @param manager
	 * @return the current session within the context.
	 */
	public static Session currentSession(BeanManager manager) {
		return getWebSocketContext(manager).currentSession();
	}

	/**
	 * Disposes the {@link WebSocketScope} for the {@link Session}.
	 * 
	 * @param manager
	 * @param session
	 */
	public static void disposeScope(BeanManager manager, Session session) {
		getWebSocketContext(manager).dispose(session);
	}
}
