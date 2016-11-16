package cito.stomp.server;

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
import cito.stomp.server.annotation.OnConnected;
import cito.stomp.server.annotation.OnDisconnect;
import cito.stomp.server.annotation.OnMessage;
import cito.stomp.server.annotation.OnSubscribe;
import cito.stomp.server.annotation.OnUnsubscribe;
import cito.stomp.server.annotation.WebSocketScope;
import cito.stomp.server.event.MessageEvent;
import cito.stomp.server.scope.WebSocketContext;
import cito.stomp.server.scope.WebSocketSessionHolder;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [12 Jul 2016]
 */
public class Extension implements javax.enterprise.inject.spi.Extension {
	private final Map<Class<? extends Annotation>, Set<ObserverMethod<MessageEvent>>> frameObservers = new ConcurrentHashMap<>();

	private WebSocketContext webSocketContext;

	/**
	 * 
	 * @param cls
	 * @param method
	 */
	private <A extends Annotation> void registerFrameObserver(Class<A> cls, ObserverMethod<MessageEvent> method) {
		Set<ObserverMethod<MessageEvent>> annotations = this.frameObservers.get(cls);
		if (annotations == null) {
			annotations = new HashSet<>();
			this.frameObservers.put(cls, annotations);
		}
		annotations.add(method);
	}

	/**
	 * 
	 * @param e
	 * @param beanManager
	 */
	public void register(@Observes ProcessObserverMethod<MessageEvent, ?> e, BeanManager beanManager) {
		final ObserverMethod<MessageEvent> method = e.getObserverMethod();
		for (Annotation a : method.getObservedQualifiers()) {
			if (a instanceof OnConnected)
				registerFrameObserver(OnConnected.class, method);
			if (a instanceof OnMessage)
				registerFrameObserver(OnMessage.class, method);
			if (a instanceof OnSubscribe)
				registerFrameObserver(OnSubscribe.class, method);
			if (a instanceof OnUnsubscribe)
				registerFrameObserver(OnUnsubscribe.class, method);
			if (a instanceof OnDisconnect)
				registerFrameObserver(OnDisconnect.class, method);
		}
	}

	/**
	 * 
	 * @param qualifier
	 * @return
	 */
	public Set<ObserverMethod<MessageEvent>> getObservers(Class<? extends Annotation> qualifier) {
		final Set<ObserverMethod<MessageEvent>> observers = this.frameObservers.get(qualifier);
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
	private static WebSocketContext getWebSocketContext(BeanManager manager) {
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
