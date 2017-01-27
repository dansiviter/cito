package cito.stomp.server;

import static cito.stomp.server.Util.getAnnotations;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.inject.Inject;

import cito.QuietClosable;
import cito.ReflectionUtil;
import cito.stomp.Glob;
import cito.stomp.server.annotation.OnConnected;
import cito.stomp.server.annotation.OnDisconnect;
import cito.stomp.server.annotation.OnSend;
import cito.stomp.server.annotation.OnSubscribe;
import cito.stomp.server.annotation.OnUnsubscribe;
import cito.stomp.server.event.MessageEvent;

/**
 * Fires off events related to destinations.
 * 
 * @author Daniel Siviter
 * @since v1.0 [27 Jul 2016]
 */
@ApplicationScoped
public class EventProducer {
	private final Map<String, String> idDestinationMap = new WeakHashMap<>();

	@Inject
	private BeanManager manager;

	/**
	 * 
	 * @param msg
	 */
	public void message(@Observes MessageEvent msg) {
		if (msg.frame().isHeartBeat()) return;

		final Extension extension = this.manager.getExtension(Extension.class);

		switch (msg.frame().getCommand()) {
		case CONNECTED: { // on client thread as it's response to CONNECT
			extension.getMessageObservers(OnConnected.class).forEach(om -> om.notify(msg));
			break;
		}
		case SEND: {
			final String destination = msg.frame().destination();
			notify(OnSend.class, extension.getMessageObservers(OnSend.class), destination, msg);
			break;
		}
		case SUBSCRIBE: {
			final String id = msg.frame().subscription();
			final String destination = msg.frame().destination();
			idDestinationMap.put(id, destination);
			notify(OnSubscribe.class, extension.getMessageObservers(OnSubscribe.class), destination, msg);
			break;
		}
		case UNSUBSCRIBE: {
			final String id = msg.frame().subscription();
			final String destination = this.idDestinationMap.remove(id);
			notify(OnUnsubscribe.class, extension.getMessageObservers(OnUnsubscribe.class), destination, msg);
			break;
		}
		case DISCONNECT: {
			extension.getMessageObservers(OnDisconnect.class).forEach(om -> om.notify(msg));
			break;
		}
		default:
			break;
		}
	}


	// --- Static Methods ---

	/**
	 * 
	 * @param annotation
	 * @param annocations
	 * @return
	 */
	private static <A extends Annotation> boolean matches(Class<A> annotation, Collection<? extends Annotation> annotations, String destination) {
		for (A a : getAnnotations(annotation, annotations)) {
			if (Glob.from(ReflectionUtil.invoke(a, "value")).matches(destination)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param annotation
	 * @param observerMethods
	 * @param destination
	 * @param evt
	 */
	private static <A extends Annotation> void notify(Class<A> annotation, Set<ObserverMethod<MessageEvent>> observerMethods, String destination, MessageEvent evt) {
		for (ObserverMethod<MessageEvent> om : observerMethods) {
			for (A a : getAnnotations(annotation, om.getObservedQualifiers())) {
				final String value = ReflectionUtil.invoke(a, "value");
				if (!Glob.from(value).matches(destination)) {
					continue;
				}
				try (QuietClosable closable = PathParamProvider.set(value)) {
					om.notify(evt);
				}
			}
		}
	}
}
