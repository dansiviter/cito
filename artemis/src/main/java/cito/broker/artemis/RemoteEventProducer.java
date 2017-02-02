package cito.broker.artemis;

import java.util.Enumeration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;

import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.slf4j.Logger;

import cito.broker.DestinationEventProducer;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [2 Feb 2017]
 */
public class RemoteEventProducer implements DestinationEventProducer, MessageListener {
	@Inject
	@ConfigProperty(name = "artemis.notificationTopic", defaultValue = "jms.topic.notifications")
	private String notificationTopic;
	@Inject
	private Logger log;
	@Inject
	private BrokerConfig config;
	@Inject
	private JMSContext ctx;
	@Inject
	private Event<cito.event.DestinationEvent> destinationEvent;

	private JMSConsumer consumer;

	/**
	 * @param init used initialise on startup of application.
	 */
	public void startup(@Observes @Initialized(ApplicationScoped.class) Object init) { }

	@PostConstruct
	public void init() {
		if (this.config.startEmbeddedBroker()) {
			return;
		}
		log.info("Sourcing DestinationEvents from remote broker.");
		final Topic topic = this.ctx.createTopic(this.notificationTopic);
		this.consumer = this.ctx.createConsumer(topic);
		this.consumer.setMessageListener(this);
	}

	@Override
	public void onMessage(Message msg) {
		System.out.println("------------------------");
		System.out.println("Received notification:");
		try {
			Enumeration propertyNames = msg.getPropertyNames();
			while (propertyNames.hasMoreElements()) {
				String propertyName = (String)propertyNames.nextElement();
				System.out.format("  %s: %s\n", propertyName, msg.getObjectProperty(propertyName));
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
		System.out.println("------------------------");
	}
	
	@PreDestroy
	public void destroy() {
		if (this.consumer == null) {
			return;
		}
		this.consumer.close();
	}
}
