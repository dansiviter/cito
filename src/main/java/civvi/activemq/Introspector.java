package civvi.activemq;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.advisory.ConsumerEvent;
import org.apache.activemq.advisory.ConsumerEventSource;
import org.apache.activemq.advisory.ConsumerListener;
import org.apache.activemq.advisory.DestinationEvent;
import org.apache.activemq.advisory.DestinationListener;
import org.apache.activemq.advisory.DestinationSource;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ConsumerId;
import org.slf4j.Logger;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [18 Jul 2016]
 */
@ApplicationScoped
public class Introspector implements DestinationListener, ConsumerListener {
	private final Map<ActiveMQDestination, ConsumerEventSource> eventSources = new ConcurrentHashMap<>();
	private final MultivaluedMap<Destination, ConsumerId> consumerMap = new MultivaluedHashMap<>();

	@Inject
	private Logger log;
	@Inject
	private ConnectionFactory factory;

	private ActiveMQConnection conn;
	private DestinationSource source;

	@PostConstruct
	public void init() {
		try {
			this.conn = (ActiveMQConnection) this.factory.createConnection();
			this.source = new DestinationSource(this.conn);
			this.source.setDestinationListener(this);
			this.source.start();
		} catch (JMSException e) {
			throw new IllegalStateException("Unable to open connection to broker!", e);
		}
	}

	@Override
	public void onDestinationEvent(DestinationEvent e) {
		try {
			if (e.isAddOperation()) {
				final ConsumerEventSource eventSource = new ConsumerEventSource(this.conn, e.getDestination());
				eventSource.setConsumerListener(this);
				eventSource.start();
				this.eventSources.put(e.getDestination(), eventSource);
			} else if (e.isRemoveOperation()) {
				final ConsumerEventSource eventSource = this.eventSources.remove(e.getDestination());
				eventSource.stop();
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void onConsumerEvent(ConsumerEvent e) {
		if (e.isStarted()) {
			this.consumerMap.add(e.getDestination(), e.getConsumerId());
		} else {
			this.consumerMap.get(e.getDestination()).remove(e.getConsumerId());
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
