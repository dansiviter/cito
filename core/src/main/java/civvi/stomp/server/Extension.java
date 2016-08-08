package civvi.stomp.server;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProcessObserverMethod;

import civvi.stomp.server.annotation.OnConnected;
import civvi.stomp.server.annotation.OnDisconnect;
import civvi.stomp.server.annotation.OnMessage;
import civvi.stomp.server.annotation.OnSubscribe;
import civvi.stomp.server.annotation.OnUnsubscribe;
import civvi.stomp.server.event.Message;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [12 Jul 2016]
 */
public class Extension implements javax.enterprise.inject.spi.Extension {
	private final Map<Class<? extends Annotation>, Set<ObserverMethod<Message>>> frameObservers = new ConcurrentHashMap<>();

	/**
	 * 
	 * @param cls
	 * @param e
	 */
	<A extends Annotation> void registerFrameObserver(Class<A> cls, ProcessObserverMethod<Message, ?> e) {
		Set<ObserverMethod<Message>> annotations = this.frameObservers.get(cls);
		if (annotations == null) {
			annotations = new HashSet<>();
			this.frameObservers.put(cls, annotations);
		}
		annotations.add(e.getObserverMethod());
	}

	/**
	 * 
	 * @param e
	 * @param beanManager
	 */
	public void register(@Observes ProcessObserverMethod<Message, ?> e, BeanManager beanManager) {
		for (Annotation a : e.getObserverMethod().getObservedQualifiers()) {
			if (a instanceof OnConnected)
				registerFrameObserver(OnConnected.class, e);
			if (a instanceof OnMessage)
				registerFrameObserver(OnMessage.class, e);
			if (a instanceof OnSubscribe)
				registerFrameObserver(OnSubscribe.class, e);
			if (a instanceof OnUnsubscribe)
				registerFrameObserver(OnUnsubscribe.class, e);
			if (a instanceof OnDisconnect)
				registerFrameObserver(OnDisconnect.class, e);
		}
	}

	/**
	 * 
	 * @param qualifier
	 * @return
	 */
	public Set<ObserverMethod<Message>> getObservers(Class<? extends Annotation> qualifier) {
		final Set<ObserverMethod<Message>> observers = this.frameObservers.get(qualifier);
		return observers == null ? Collections.emptySet() : observers;
	}
}
