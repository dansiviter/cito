package cito.stomp.server;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import cito.stomp.ext.Serialiser;
import cito.stomp.server.annotation.OnConnected;
import cito.stomp.server.annotation.OnDisconnect;
import cito.stomp.server.annotation.OnMessage;
import cito.stomp.server.annotation.OnSubscribe;
import cito.stomp.server.annotation.OnUnsubscribe;
import cito.stomp.server.event.Message;

/**
 * Fires off events related to destinations.
 * 
 * @author Daniel Siviter
 * @since v1.0 [27 Jul 2016]
 */
@ApplicationScoped
public class EventProducer {
	@Inject
	private BeanManager manager;
	@Inject
	private DestinationMatcher matcher;

	/**
	 * 
	 * @param msg
	 */
	public void message(@Observes Message msg) {
		if (msg.frame.isHeartBeat()) return;

		switch (msg.frame.getCommand()) {
		case CONNECTED: {
			getExtension().getObservers(OnConnected.class).forEach(e -> e.notify(msg));
			break;
		}
		case MESSAGE: {
			final String destination = msg.frame.getDestination();
			getExtension().getObservers(OnMessage.class).stream().filter(
					e ->  this.matcher.matches(getAnnotation(OnMessage.class, e.getObservedQualifiers()).value(), destination)).forEach(
							e -> e.notify(msg));
			break;
		}
		case SUBSCRIBE: {
			final String destination = msg.frame.getDestination();
			getExtension().getObservers(OnSubscribe.class).stream().filter(
					e ->  this.matcher.matches(getAnnotation(OnSubscribe.class, e.getObservedQualifiers()).value(), destination)).forEach(
							e -> e.notify(msg));
			break;
		}
		case UNSUBSCRIBE: {
			final String destination = msg.frame.getDestination();
			getExtension().getObservers(OnUnsubscribe.class).stream().filter(
					e ->  this.matcher.matches(getAnnotation(OnUnsubscribe.class, e.getObservedQualifiers()).value(), destination)).forEach(
							e -> e.notify(msg));
			break;
		}
		case DISCONNECT: {
			getExtension().getObservers(OnDisconnect.class).forEach(e -> e.notify(msg));
			break;
		}
		default:
			break;
		}
	}

	private Extension getExtension() {
		return this.manager.getExtension(Extension.class);
	}


	// --- Static Methods ---


	/**
	 * 
	 * @param annotation
	 * @param annocations
	 * @return
	 */
	private static <A extends Annotation> A getAnnotation(Class<A> annotation, Collection<? extends Annotation> annocations) {
		for (Annotation a : annocations) {
			if (annotation.isAssignableFrom(annotation)) {
				return annotation.cast(a);
			}
		}
		return null;
	}
}
