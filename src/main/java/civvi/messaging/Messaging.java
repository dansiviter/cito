package civvi.messaging;

import java.lang.annotation.Annotation;
import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import civvi.messaging.annotation.OnConnect;
import civvi.messaging.annotation.OnMessage;
import civvi.messaging.annotation.OnSubscribe;
import civvi.stomp.Frame;

/**
 * Entry point into the messaging.
 * 
 * @author Daniel Siviter
 * @since v1.0 [12 Jul 2016]
 */
@ApplicationScoped
public class Messaging {
	@Inject
	private BeanManager beanManager;

	@Inject @OnSubscribe
	private Event<Frame> onMessageEvent;

	public void init(@Observes @Initialized(ApplicationScoped.class) ServletContext context) {
		System.out.println("### Hellow world ###" + geExtension().getFrameObservers());
		fireOnSubscribe("hello", new Frame());
	}

	private MessagingExtension geExtension() {
		return this.beanManager.getExtension(MessagingExtension.class);
	}

	/**
	 * 
	 * @param frame
	 */
	public void fireOnConnect(Frame frame) {
		geExtension().getFrameObservers().get(OnConnect.class).forEach((e) -> e.notify(frame));
	}

	/**
	 * 
	 * @param topic
	 * @param frame
	 */
	public void fireOnMessage(String topic, Frame frame) {
		geExtension().getFrameObservers().get(OnMessage.class).stream().filter(
				(e) ->  matches(topic, getAnnotation(OnMessage.class, e.getObservedQualifiers()).value())).forEach(
						(e) -> e.notify(frame));
	}

	/**
	 * 
	 * @param topic
	 * @param frame
	 */
	public void fireOnSubscribe(String topic, Frame frame) {
		geExtension().getFrameObservers().get(OnSubscribe.class).stream().filter(
				(e) ->  matches(topic, getAnnotation(OnSubscribe.class, e.getObservedQualifiers()).value())).forEach(
						(e) -> e.notify(frame));
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
				return (A) a;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param topic
	 * @param test
	 * @return
	 */
	private boolean matches(String topic, String test) {
		// TODO add logic
		return true;
	}
}
