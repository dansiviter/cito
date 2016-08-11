package flngr.artemis;

import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.jms.server.config.ConnectionFactoryConfiguration;
import org.apache.activemq.artemis.jms.server.config.JMSConfiguration;

/**
 * A customiser for the broker.
 * 
 * @author Daniel Siviter
 * @since v1.0 [4 Aug 2016]
 */
public interface BrokerCustomiser {
	/**
	 * 
	 * @param config
	 * @param jmsConfig
	 * @param cfConfig
	 */
	void customise(Configuration config, JMSConfiguration jmsConfig, ConnectionFactoryConfiguration cfConfig);
}
