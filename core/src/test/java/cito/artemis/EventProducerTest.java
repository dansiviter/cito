package cito.artemis;

import static org.mockito.Mockito.verifyNoMoreInteractions;

import javax.enterprise.event.Event;

import org.apache.activemq.artemis.api.core.management.CoreNotificationType;
import org.apache.activemq.artemis.core.server.management.Notification;
import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;
import org.apache.activemq.artemis.jms.server.management.JMSNotificationType;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 * Unit test for {@link EventProducer}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [17 Jan 2017]
 */
@RunWith(MockitoJUnitRunner.class)
public class EventProducerTest {
	@Mock
	private Logger log;
	@Mock
	private EmbeddedJMS broker;
	@Mock
	private Event<cito.DestinationEvent> destinationEvent;

	@InjectMocks
	private EventProducer eventProducer;

	@Test
	public void unknownEvent() {
		for (CoreNotificationType type : CoreNotificationType.values()) {
			this.eventProducer.onNotification(new Notification("", type, null));
		}
		this.eventProducer.onNotification(new Notification("", JMSNotificationType.CONNECTION_FACTORY_CREATED, null));
		this.eventProducer.onNotification(new Notification("", JMSNotificationType.CONNECTION_FACTORY_DESTROYED, null));
	}

	@After
	public void after() {
		verifyNoMoreInteractions(this.log, this.broker, this.destinationEvent);
	}
}
