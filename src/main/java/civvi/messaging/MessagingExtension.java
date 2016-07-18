package civvi.messaging;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProcessObserverMethod;

import civvi.messaging.annotation.OnMessage;
import civvi.messaging.annotation.OnSubscribe;
import civvi.stomp.Frame;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [12 Jul 2016]
 */
public class MessagingExtension implements Extension {
	private final Map<Class<? extends Annotation>, Set<ObserverMethod<Frame>>> frameObservers = new HashMap<>();

	
	/**
	 * 
	 * @param cls
	 * @param e
	 */
	<A extends Annotation> void registerFrameObserver(Class<A> cls, ProcessObserverMethod<Frame, ?> e) {
		Set<ObserverMethod<Frame>> annotations = this.frameObservers.get(cls);
		if (annotations == null) {
			annotations = new HashSet<>();
			this.frameObservers.put(cls, annotations);
		}
		annotations.add(e.getObserverMethod());
	}

	public void register(@Observes ProcessObserverMethod<Frame, ?> e, BeanManager beanManager) {
		for (Annotation a : e.getObserverMethod().getObservedQualifiers()) {
			if (a instanceof OnMessage)
				registerFrameObserver(OnMessage.class, e);
			if (a instanceof OnSubscribe)
				registerFrameObserver(OnSubscribe.class, e);
			if (a instanceof OnSubscribe)
				registerFrameObserver(OnSubscribe.class, e);
		}
	}
	
	Map<Class<? extends Annotation>, Set<ObserverMethod<Frame>>> getFrameObservers() {
		return frameObservers;
	}
}
