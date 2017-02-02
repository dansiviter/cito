package cito.broker.artemis;

import org.apache.activemq.artemis.core.config.Configuration;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [2 Feb 2017]
 */
public abstract class BrokerConfigAdapter implements BrokerConfig {

	@Override
	public String getUsername() {
		return null;
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public String getUrl() {
		return null;
	}

	@Override
	public String getHost() {
		return null;
	}

	@Override
	public Integer getPort() {
		return null;
	}

	@Override
	public String getConnectorFactory() {
		return null;
	}

	@Override
	public boolean startEmbeddedBroker() {
		return false;
	}

	@Override
	public boolean isHa() {
		return false;
	}

	@Override
	public boolean hasAuthentication() {
		return false;
	}

	@Override
	public Configuration getEmbeddedConfiguration() {
		return null;
	}
}
