/*
 * Copyright 2016-2017 Daniel Siviter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cito.broker.artemis;

import static cito.Util.getAnnotations;
import static org.apache.activemq.artemis.api.core.management.ManagementHelper.HDR_NOTIFICATION_TYPE;
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
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;

import org.apache.activemq.artemis.api.core.management.CoreNotificationType;
import org.apache.activemq.artemis.api.core.management.NotificationType;
import org.apache.activemq.artemis.jms.server.management.JMSNotificationType;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.slf4j.Logger;

import cito.Glob;
import cito.PathParamProducer;
import cito.QuietClosable;
import cito.ReflectionUtil;
import cito.annotation.OnAdded;
import cito.annotation.OnRemoved;
import cito.broker.DestinationChangedHolder;
import cito.event.DestinationChanged;
import cito.event.DestinationChanged.Type;
import cito.server.Extension;

/**
 * Produces {@link DestinationChanged}s based on the Artemis notification topic. By default this will listen to
 * {@code jmx.topic.notifications} which is the one setup for the embedded broker. However, if you're using a remote
 * instance be sure to check what is used on that and set {@code artemis.notificationTopic} property.
 * 
 * @author Daniel Siviter
 * @since v1.0 [2 Feb 2017]
 */
@ApplicationScoped
public class EventProducer implements MessageListener {
	private static final Collection<JMSNotificationType> ALL = EnumSet.of(TOPIC_CREATED, TOPIC_DESTROYED, QUEUE_CREATED, QUEUE_DESTROYED);
	private static final Collection<JMSNotificationType> CREATED = EnumSet.of(TOPIC_CREATED, QUEUE_CREATED);
	private static final Collection<JMSNotificationType> TOPIC = EnumSet.of(TOPIC_CREATED, TOPIC_DESTROYED);

	@Inject
	private BeanManager manager;
	@Inject
	@ConfigProperty(name = "artemis.notificationTopic", defaultValue = "notifications")
	private String notificationTopic;
	@Inject
	private Logger log;
	@Inject
	private JMSContext ctx;
	@Inject
	private Event<cito.event.DestinationChanged> destinationEvent;

	private JMSConsumer consumer;

	/**
	 * @param init used initialise on startup of application.
	 */
	public void startup(@Observes @Initialized(ApplicationScoped.class) Object init) { }

	@PostConstruct
	public void init() {
		log.info("Sourcing DestinationEvents from remote broker.");
		final Topic topic = this.ctx.createTopic(this.notificationTopic);
		this.consumer = this.ctx.createConsumer(topic);
		this.consumer.setMessageListener(this);
	}

	@Override
	public void onMessage(Message msg) {
		try {
			final NotificationType notifType = valueofNotificationType(msg.getStringProperty(HDR_NOTIFICATION_TYPE.toString()));
			if (!ALL.contains(notifType)) {
				return;
			}

			String destination = msg.getStringProperty(JMSNotificationType.MESSAGE.toString());
			destination = (TOPIC.contains(notifType) ? "/topic/" : "/queue/" ) + destination;
			final Type type = CREATED.contains(notifType) ? Type.ADDED : Type.REMOVED;

			this.log.info("Destination changed. [type={},destination={}]", type, destination);
			final cito.event.DestinationChanged evt = new cito.event.DestinationChanged(type, destination);
			try (QuietClosable c = DestinationChangedHolder.set(evt)) {
				this.destinationEvent.fire(evt);
			}
		} catch (JMSException | RuntimeException e) {
			this.log.error("Unable to process notification!", e);
		}
	}

	/**
	 * 
	 * @param msg
	 */
	public void message(@Observes DestinationChanged msg) {
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
		if (this.consumer == null) {
			return;
		}
		this.consumer.close();
	}


	// --- Static Methods ---

	/**
	 * 
	 * @param annotation
	 * @param observerMethods
	 * @param destination
	 * @param evt
	 */
	private static <A extends Annotation> void notify(Class<A> annotation, Set<ObserverMethod<DestinationChanged>> observerMethods, String destination, DestinationChanged evt) {
		for (ObserverMethod<DestinationChanged> om : observerMethods) {
			for (A a : getAnnotations(annotation, om.getObservedQualifiers())) {
				final String value = ReflectionUtil.invoke(a, "value");
				if (!Glob.from(value).matches(destination)) {
					continue;
				}
				try (QuietClosable closable = PathParamProducer.set(value)) {
					om.notify(evt);
				}
			}
		}
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	private static NotificationType valueofNotificationType(String name) {
		for (JMSNotificationType jmsType : JMSNotificationType.values()) {
			if (name.equals(jmsType.name())) {
				return jmsType;
			}
		}
		for (CoreNotificationType coreType : CoreNotificationType.values()) {
			if (name.equals(coreType.name())) {
				return coreType;
			}
		}
		throw new IllegalArgumentException(
				"No enum constant " + NotificationType.class.getCanonicalName() + "." + name);
	}
}
