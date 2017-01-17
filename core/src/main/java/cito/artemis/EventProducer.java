package cito.artemis;

import static cito.stomp.server.annotation.Qualifiers.onDestinaton;
import static org.apache.activemq.artemis.jms.server.management.JMSNotificationType.MESSAGE;
import static org.apache.activemq.artemis.jms.server.management.JMSNotificationType.QUEUE_CREATED;
import static org.apache.activemq.artemis.jms.server.management.JMSNotificationType.QUEUE_DESTROYED;
import static org.apache.activemq.artemis.jms.server.management.JMSNotificationType.TOPIC_CREATED;
import static org.apache.activemq.artemis.jms.server.management.JMSNotificationType.TOPIC_DESTROYED;

import java.util.Collection;
import java.util.EnumSet;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.activemq.artemis.core.server.management.Notification;
import org.apache.activemq.artemis.core.server.management.NotificationListener;
import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;
import org.apache.activemq.artemis.jms.server.management.JMSNotificationType;
import org.slf4j.Logger;

import cito.DestinationEvent.DestinationType;
import cito.DestinationEvent.Type;

/**
 * Produces events based on the state of the broker.
 * 
 * @author Daniel Siviter
 * @since v1.0 [18 Jul 2016]
 */
@ApplicationScoped
public class EventProducer implements NotificationListener {
	private static final Collection<JMSNotificationType> ALL = EnumSet.of(TOPIC_CREATED, TOPIC_DESTROYED, QUEUE_CREATED, QUEUE_DESTROYED);
	private static final Collection<JMSNotificationType> CREATED = EnumSet.of(TOPIC_CREATED, QUEUE_CREATED);
	private static final Collection<JMSNotificationType> TOPIC = EnumSet.of(TOPIC_DESTROYED, TOPIC_DESTROYED);

	@Inject
	private Logger log;
	@Inject
	private EmbeddedJMS broker;
	@Inject
	private Event<cito.DestinationEvent> destinationEvent;

	/**
	 * @param init used initialise on startup of application.
	 */
	public void startup(@Observes @Initialized(ApplicationScoped.class) Object init) { }

	@PostConstruct
	public void init() {
		this.broker.getActiveMQServer().getManagementService().addNotificationListener(this);
	}

	@Override
	public void onNotification(Notification notif) {
		if (!ALL.contains(notif.getType())) {
			return;
		}

		final String destination = notif.getProperties().getSimpleStringProperty(MESSAGE).toString();
		final Type type = CREATED.contains(notif.getType()) ? Type.ADDED : Type.REMOVED;
		final DestinationType destinationType = TOPIC.contains(notif.getType()) ? DestinationType.TOPIC : DestinationType.QUEUE;

		this.log.info("Destination changed. [type={},destinationType={},destination={}]", type, destinationType, destination);
		this.destinationEvent.select(onDestinaton(type)).fire(new cito.DestinationEvent(type, destinationType, destination));
	}

	@PreDestroy
	public void destroy() {
		this.broker.getActiveMQServer().getManagementService().removeNotificationListener(this);
	}
}
