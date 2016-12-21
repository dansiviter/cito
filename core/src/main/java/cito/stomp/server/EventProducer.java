package cito.stomp.server;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

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
	@Inject
	private SessionRegistry sessionRegistry;

	/**
	 * 
	 * @param msg
	 */
	public void message(@Observes MessageEvent msg) {
		if (msg.frame().isHeartBeat()) return;

		final Extension extension = this.manager.getExtension(Extension.class);

		switch (msg.frame().getCommand()) {
		case CONNECTED: { // on client thread as it's response to CONNECT
			extension.getObservers(OnConnected.class).forEach(e -> e.notify(msg));
			break;
		}
		case SEND: {
			final String destination = msg.frame().destination();
			extension.getObservers(OnSend.class).stream().filter(
					e -> matches(OnSend.class, e.getObservedQualifiers(), destination)).forEach(
							e -> e.notify(msg));
			break;
		}
		case SUBSCRIBE: {
			final String id = msg.frame().subscription();
			final String destination = msg.frame().destination();
			idDestinationMap.put(id, destination);
			extension.getObservers(OnSubscribe.class).stream().filter(
					e -> matches(OnSubscribe.class, e.getObservedQualifiers(), destination)).forEach(
							e -> e.notify(msg));
			break;
		}
		case UNSUBSCRIBE: {
			final String id = msg.frame().subscription();
			final String destination = this.idDestinationMap.remove(id);
			extension.getObservers(OnUnsubscribe.class).stream().filter(
					e -> matches(OnUnsubscribe.class, e.getObservedQualifiers(), destination)).forEach(
							e -> e.notify(msg));
			break;
		}
		case DISCONNECT: {
			extension.getObservers(OnDisconnect.class).forEach(e -> e.notify(msg));
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
	@SuppressWarnings("unchecked")
	private static <A extends Annotation> A[] getAnnotations(Class<A> annotation, Collection<? extends Annotation> annotations) {
		final Collection<A> found = new ArrayList<>();
		for (Annotation a : annotations) {
			if (annotation.isAssignableFrom(annotation)) {
				found.add(annotation.cast(a));
			}
		}
		return found.toArray((A[]) Array.newInstance(annotation, 0));
	}

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
}
