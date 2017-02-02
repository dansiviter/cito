package cito.broker.artemis;

import java.util.Collections;
import java.util.Map;

import javax.enterprise.inject.Vetoed;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.invm.TransportConstants;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [2 Feb 2017]
 */
@Vetoed
public class DefaultBrokerConfig extends BrokerConfigAdapter {
	@Override
	public String getConnectorFactory() {
		return IN_VM_CONNECTOR;
	}

	@Override
	public boolean startEmbeddedBroker() {
		return true;
	}

	@Override
	public Configuration getEmbeddedConfiguration() {
		final Map<String, Object> params = Collections.singletonMap(TransportConstants.SERVER_ID_PROP_NAME, "1");
		return new ConfigurationImpl()
				.setSecurityEnabled(false)
				.setPersistenceEnabled(false)
				.setJMXManagementEnabled(false)
				.addAcceptorConfiguration(new TransportConfiguration(InVMAcceptorFactory.class.getName(), params));
	}
}
