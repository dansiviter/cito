package cito.broker.artemis;

import static cito.Util.getAnnotations;
import static org.apache.activemq.artemis.jms.server.management.JMSNotificationType.MESSAGE;
import static org.apache.activemq.artemis.jms.server.management.JMSNotificationType.QUEUE_CREATED;
import static org.apache.activemq.artemis.jms.server.management.JMSNotificationType.QUEUE_DESTROYED;
import static org.apache.activemq.artemis.jms.server.management.JMSNotificationType.TOPIC_CREATED;
import static org.apache.activemq.artemis.jms.server.management.JMSNotificationType.TOPIC_DESTROYED;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.inject.Inject;

import org.apache.activemq.artemis.core.server.management.Notification;
import org.apache.activemq.artemis.core.server.management.NotificationListener;
import org.apache.activemq.artemis.jms.server.JMSServerManager;
import org.apache.activemq.artemis.jms.server.management.JMSNotificationType;
import org.slf4j.Logger;

import cito.Glob;
import cito.PathParamProvider;
import cito.QuietClosable;
import cito.ReflectionUtil;
import cito.annotation.OnAdded;
import cito.annotation.OnRemoved;
import cito.broker.DestinationEventProducer;
import cito.event.DestinationEvent;
import cito.event.DestinationEvent.Type;
import cito.server.Extension;

/**
 * Produces events based on the state of the embedded broker.
 * 
 * @author Daniel Siviter
 * @since v1.0 [18 Jul 2016]
 */
@ApplicationScoped
public class EmbeddedEventProducer implements NotificationListener, DestinationEventProducer {
	private static final Collection<JMSNotificationType> ALL = EnumSet.of(TOPIC_CREATED, TOPIC_DESTROYED, QUEUE_CREATED, QUEUE_DESTROYED);
	private static final Collection<JMSNotificationType> CREATED = EnumSet.of(TOPIC_CREATED, QUEUE_CREATED);
	private static final Collection<JMSNotificationType> TOPIC = EnumSet.of(TOPIC_CREATED, TOPIC_DESTROYED);

	@Inject
	private BeanManager manager;
	@Inject
	private Logger log;
	@Inject
	private JMSServerManager serverManager;
	@Inject
	private Event<cito.event.DestinationEvent> destinationEvent;

	/**
	 * @param init used initialise on startup of application.
	 */
	public void startup(@Observes @Initialized(ApplicationScoped.class) Object init) { }

	@PostConstruct
	public void init() {
		if (this.serverManager == null) {
			return;
		}
		log.info("Sourcing DestinationEvents from embedded broker.");
		this.serverManager.getActiveMQServer().getManagementService().addNotificationListener(this);
	}

	@Override
	public void onNotification(Notification notif) {
		if (!ALL.contains(notif.getType())) {
			return;
		}

		String destination = notif.getProperties().getSimpleStringProperty(MESSAGE).toString();
		destination = (TOPIC.contains(notif.getType()) ? "/topic/" : "/queue/" ) + destination;
		final Type type = CREATED.contains(notif.getType()) ? Type.ADDED : Type.REMOVED;

		this.log.info("Destination changed. [type={},destination={}]", type, destination);
		final cito.event.DestinationEvent evt = new cito.event.DestinationEvent(type, destination);
		try (QuietClosable c = DestinationEventProducer.set(evt)) {
			this.destinationEvent.fire(evt);
		}
	}

	/**
	 * 
	 * @param msg
	 */
	public void message(@Observes DestinationEvent msg) {
		final Extension extension = this.manager.getExtension(Extension.class);

		final String destination = msg.getDestination();
		switch (msg.getType()) {
		case ADDED: {
			notify(OnAdded.class, extension.getDestinationObservers(OnAdded.class), destination, msg);
			break;
		}
		case REMOVED: {
			notify(OnRemoved.class, extension.getDestinationObservers(OnRemoved.class), destination, msg);
			break;
		}
		default:
			break;
		}
	}

	@PreDestroy
	public void destroy() {
		if (this.serverManager == null) {
			return;
		}
		this.serverManager.getActiveMQServer().getManagementService().removeNotificationListener(this);
	}

	/**
	 * 
	 * @param annotation
	 * @param observerMethods
	 * @param destination
	 * @param evt
	 */
	private static <A extends Annotation> void notify(Class<A> annotation, Set<ObserverMethod<DestinationEvent>> observerMethods, String destination, DestinationEvent evt) {
		for (ObserverMethod<DestinationEvent> om : observerMethods) {
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
