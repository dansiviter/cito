package civvi.activemq;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [18 Jul 2016]
 */
@ApplicationScoped
public class Broker {
//	@Inject
//	@ConfigProperty(name = "activemq.port", defaultValue = "61613")
	private int port = 61613;
//	@Inject
//	@ConfigProperty(name = "activemq.gracePeriodmultiplier", defaultValue = "2")
	private float gracePeriodmultiplier = 1.5f;

	private BrokerService broker;

	// small hack to force startup
	public void init(@Observes @Initialized(ApplicationScoped.class) Object init) { }

	@PostConstruct
	public void init() {
		try {
			this.broker = new BrokerService();
			this.broker.setUseShutdownHook(false);
			this.broker.setPersistent(false);
			this.broker.getManagementContext().setCreateConnector(false);
			this.broker.addConnector("stomp+nio://localhost:" + port + "?transport.hbGracePeriodMultiplier=" + gracePeriodmultiplier);
			this.broker.setSchedulePeriodForDestinationPurge(60 * 1_000);
			final PolicyEntry defaultEntry = new PolicyEntry();
			defaultEntry.setGcInactiveDestinations(true); // purge destination
			final PolicyMap policyMap = new PolicyMap();
			policyMap.setDefaultEntry(defaultEntry);
			this.broker.setDestinationPolicy(policyMap);
			this.broker.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@PreDestroy
	public void destroy() {
		try {
			this.broker.stop();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Produces @ApplicationScoped
	public ConnectionFactory connectionFactory() {
		return new ActiveMQConnectionFactory("vm://localhost?create=false");
	}
	//
	//	@Produces @Inject
	//	public ActiveMQConnection connection(ActiveMQConnectionFactory connectionFactory) throws JMSException {
	//		return (ActiveMQConnection) connectionFactory.createConnection();
	//	}

}
