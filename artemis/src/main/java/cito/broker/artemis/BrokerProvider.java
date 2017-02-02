package cito.broker.artemis;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.jms.JMSContext;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.api.jms.JMSFactoryType;
import org.apache.activemq.artemis.core.remoting.impl.netty.TransportConstants;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.ActiveMQServers;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.server.JMSServerManager;
import org.apache.activemq.artemis.jms.server.impl.JMSServerManagerImpl;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [2 Feb 2017]
 */
public class BrokerProvider {
	@Inject
	private BrokerConfig config;

	@Produces
	@ApplicationScoped
	private JMSServerManager jmsServerManager;
	@Produces
	@ApplicationScoped
	private ActiveMQConnectionFactory activeMQConnectionFactory;

	@PostConstruct
	public void init() {
		if (this.config.startEmbeddedBroker()) {
			try {
				ActiveMQServer activeMQServer = ActiveMQServers.newActiveMQServer(this.config.getEmbeddedConfiguration(), false);
				this.jmsServerManager = new JMSServerManagerImpl(activeMQServer);
				this.jmsServerManager.start();
			} catch (Exception e) {
				throw new IllegalStateException("Unable to start embedded JMS", e);
			}
		}

		try {
			this.activeMQConnectionFactory = createConnectionFactory();
		} catch (Exception e) {
			throw new IllegalStateException("Unable to connect to remote server", e);
		}
	}

	@Produces
	@ApplicationScoped
	public JMSContext createJMSContext() {
		return this.activeMQConnectionFactory.createContext();
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	private ActiveMQConnectionFactory createConnectionFactory() throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put(org.apache.activemq.artemis.core.remoting.impl.invm.TransportConstants.SERVER_ID_PROP_NAME, "1");
		final ActiveMQConnectionFactory activeMQConnectionFactory;
		if (config.getUrl() != null) {
			activeMQConnectionFactory = ActiveMQJMSClient.createConnectionFactory(config.getUrl(), null);
		} else {
			if (config.getHost() != null) {
				params.put(TransportConstants.HOST_PROP_NAME, config.getHost());
				params.put(TransportConstants.PORT_PROP_NAME, config.getPort());
			}
			if (config.isHa()) {
				activeMQConnectionFactory = ActiveMQJMSClient.createConnectionFactoryWithHA(JMSFactoryType.CF, new TransportConfiguration(config.getConnectorFactory(), params));
			} else {
				activeMQConnectionFactory = ActiveMQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, new TransportConfiguration(config.getConnectorFactory(), params));
			}
		}
		if (config.hasAuthentication()) {
			activeMQConnectionFactory.setUser(config.getUsername());
			activeMQConnectionFactory.setPassword(config.getPassword());
		}
		return activeMQConnectionFactory.disableFinalizeChecks();
	}

	@PreDestroy
	public void destroy() {
		if (this.config.startEmbeddedBroker()) {
			try {
				this.jmsServerManager.stop();
			} catch (Exception e) {
				throw new IllegalStateException("Unable to stop embedded JMS", e);
			}
		}
	}
}
