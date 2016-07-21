package civvi.activemq;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Destination;
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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [18 Jul 2016]
 */
//@ApplicationScoped
public class Introspector implements DestinationListener, ConsumerListener {
	private final BiMap<String, String> sessionLookup = HashBiMap.create();
	private final MultivaluedMap<String, String> sessionMap = new MultivaluedHashMap<>();

	private final Map<ActiveMQDestination, ConsumerEventSource> eventSources = new ConcurrentHashMap<>();
	private final MultivaluedMap<Destination, ConsumerId> consumerMap = new MultivaluedHashMap<>();


//	@Inject
	private ActiveMQConnection conn;

	private DestinationSource source;

//	@PostConstruct
	public void init() throws Exception {
		this.source = new DestinationSource(this.conn);
		this.source.setDestinationListener(this);
		this.source.start();
	}

//	public Map<String, Set<String>> destinations(String key) {
//
//	}

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
}
