package civvi.activemq;

import javax.enterprise.event.Event;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.advisory.DestinationEvent;
import org.apache.activemq.advisory.DestinationListener;

import civvi.DestinationChangedLiteral;
import civvi.DestinationEvent.Type;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [18 Jul 2016]
 */
//@ApplicationScope
public class EventProducer implements DestinationListener {

//	@Inject
	private ActiveMQConnection conn;
//	@Inject
	private Event<civvi.DestinationEvent> destinationEvent;

//	@PostConstruct
	public void init() throws JMSException {
		this.conn.getDestinationSource().setDestinationListener(this);
	}

//	@PreDestroy
	public void destroy() throws JMSException {
		this.conn.close();
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
}
