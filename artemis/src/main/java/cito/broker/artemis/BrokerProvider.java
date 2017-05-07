/*
 * Copyright 2016-2017 Daniel Siviter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cito.broker.artemis;

import java.util.Collections;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSRuntimeException;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.api.jms.JMSFactoryType;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.remoting.impl.netty.TransportConstants;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.server.config.JMSConfiguration;
import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;
import org.slf4j.Logger;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [2 Feb 2017]
 */
@ApplicationScoped
public class BrokerProvider {
	@Inject
	private Logger log;
	@Inject
	private BrokerConfig config;

	@Produces
	@ApplicationScoped
	private EmbeddedJMS embeddedJMS;
	@Produces
	@ApplicationScoped
	private ActiveMQConnectionFactory connectionFactory;
	@Produces
	@ApplicationScoped
	private Configuration artemisConfig;

	@PostConstruct
	public void init() {
		if (this.config.startEmbeddedBroker()) {
			this.log.info("Starting embedded broker.");
			try {
				this.artemisConfig = this.config.getConfiguration();
				final JMSConfiguration jmsConfig = this.config.getJmsConfig();
				this.embeddedJMS = new EmbeddedJMS().setConfiguration(this.artemisConfig).setJmsConfiguration(jmsConfig);
				this.embeddedJMS.start();
			} catch (Exception e) {
				throw new JMSRuntimeException("Unable to start embedded JMS", null, e);
			}
		}

		try {
			this.connectionFactory = createConnectionFactory(this.config);
		} catch (Exception e) {
			throw new JMSRuntimeException("Unable to connect to remote server", null, e);
		}
	}

	@Produces @Dependent
	public JMSContext createJMSContext() {
		return this.connectionFactory.createContext();
	}

	@PreDestroy
	public void destroy() {
		if (this.config.startEmbeddedBroker()) {
			this.log.info("Stopping embedded broker.");
			try {
				this.embeddedJMS.stop();
			} catch (Exception e) {
				throw new JMSRuntimeException("Unable to stop embedded JMS", null, e);
			}
		}
	}

	/**
	 * @param connectionFactory the connection factory to dispose.
	 */
	public void dispose(@Disposes ActiveMQConnectionFactory connectionFactory) {
		connectionFactory.close();
	}


	// --- Static Methods ---

	/**
	 * 
	 * @param config
	 * @return
	 * @throws Exception
	 */
	private static ActiveMQConnectionFactory createConnectionFactory(BrokerConfig config) throws Exception {
		Map<String, Object> params = Collections.singletonMap(
				org.apache.activemq.artemis.core.remoting.impl.invm.TransportConstants.SERVER_ID_PROP_NAME, "1");
		final ActiveMQConnectionFactory connectionFactory;
		if (config.getUrl() != null) {
			connectionFactory = ActiveMQJMSClient.createConnectionFactory(config.getUrl(), null);
		} else {
			if (config.getHost() != null) {
				params.put(TransportConstants.HOST_PROP_NAME, config.getHost());
				params.put(TransportConstants.PORT_PROP_NAME, config.getPort());
			}
			if (config.isHa()) {
				connectionFactory = ActiveMQJMSClient.createConnectionFactoryWithHA(JMSFactoryType.CF, new TransportConfiguration(config.getConnectorFactory(), params));
			} else {
				connectionFactory = ActiveMQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, new TransportConfiguration(config.getConnectorFactory(), params));
			}
		}
		if (config.hasAuthentication()) {
			connectionFactory.setUser(config.getUsername());
			connectionFactory.setPassword(config.getPassword());
		}
		return connectionFactory.disableFinalizeChecks();
	}
}
