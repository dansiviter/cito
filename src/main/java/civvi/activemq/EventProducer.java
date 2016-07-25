package civvi.activemq;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.advisory.DestinationEvent;
import org.apache.activemq.advisory.DestinationListener;
import org.slf4j.Logger;

import civvi.DestinationChangedLiteral;
import civvi.DestinationEvent.Type;

/**
 * Produces events based on the state of the broker.
 * 
 * @author Daniel Siviter
 * @since v1.0 [18 Jul 2016]
 */
@ApplicationScoped
public class EventProducer implements DestinationListener {
	@Inject
	private Logger log;
	@Inject
	private ConnectionFactory factory;
	@Inject
	private Event<civvi.DestinationEvent> destinationEvent;

	private ActiveMQConnection conn;

	@PostConstruct
	public void init() {
		try {
			this.conn = (ActiveMQConnection) this.factory.createConnection();
			this.conn.getDestinationSource().setDestinationListener(this);
		} catch (JMSException e) {
			throw new IllegalStateException("Unable to open connection to broker!", e);
		}
	}

	@Override
	public void onDestinationEvent(DestinationEvent e) {
		final String destination = e.getDestination().getPhysicalName();
		if (e.isAddOperation()) {
			this.destinationEvent.select(new DestinationChangedLiteral(Type.ADDED))
					.fire(new civvi.DestinationEvent(Type.ADDED, destination));
		} else if (e.isRemoveOperation()) {
			this.destinationEvent.select(new DestinationChangedLiteral(Type.REMOVED))
					.fire(new civvi.DestinationEvent(Type.REMOVED, destination));
		}
	}

	@PreDestroy
	public void destroy() {
		try {
			this.conn.close();
		} catch (JMSException e) {
			this.log.warn("Unable to close connection!", e);
		}
	}
}
