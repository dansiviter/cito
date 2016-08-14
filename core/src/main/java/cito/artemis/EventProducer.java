package cito.artemis;

import static cito.DestinationEvent.DestinationType.QUEUE;
import static cito.DestinationEvent.DestinationType.TOPIC;
import static cito.stomp.server.annotation.Qualifiers.onDestinaton;
import static org.apache.activemq.artemis.jms.server.management.JMSNotificationType.MESSAGE;
import static org.apache.activemq.artemis.jms.server.management.JMSNotificationType.QUEUE_CREATED;
import static org.apache.activemq.artemis.jms.server.management.JMSNotificationType.QUEUE_DESTROYED;
import static org.apache.activemq.artemis.jms.server.management.JMSNotificationType.TOPIC_CREATED;
import static org.apache.activemq.artemis.jms.server.management.JMSNotificationType.TOPIC_DESTROYED;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.activemq.artemis.core.server.management.Notification;
import org.apache.activemq.artemis.core.server.management.NotificationListener;
import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;
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
	@Inject
	private Logger log;
	@Inject
	private EmbeddedJMS broker;
	@Inject
	private Event<cito.DestinationEvent> destinationEvent;

	public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
		this.broker.getActiveMQServer().getManagementService().addNotificationListener(this);
	}

	@Override
	public void onNotification(Notification notif) {
		if (notif.getType() != TOPIC_CREATED && notif.getType() != QUEUE_CREATED &&
				notif.getType() != TOPIC_DESTROYED || notif.getType() != QUEUE_DESTROYED)
		{
			return;
		}

		final String destination = notif.getProperties().getSimpleStringProperty(MESSAGE).toString();
		final Type type = notif.getType() == TOPIC_CREATED || notif.getType() == QUEUE_CREATED ? Type.ADDED : Type.REMOVED;
		final DestinationType destinationType = notif.getType() == QUEUE_DESTROYED || notif.getType() == QUEUE_CREATED ? QUEUE : TOPIC;

		this.log.info("Destination changed. [type={},destinationType={},destination={}]", type, destinationType, destination);
		this.destinationEvent.select(onDestinaton(type)).fire(new cito.DestinationEvent(type, destinationType, destination));
	}

	@PreDestroy
	public void destroy() {
		this.broker.getActiveMQServer().getManagementService().removeNotificationListener(this);
	}
}
