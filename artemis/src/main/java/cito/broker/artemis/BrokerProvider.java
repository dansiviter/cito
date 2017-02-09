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
	private JMSServerManager jmsServerManager;
	@Produces
	@ApplicationScoped
	private ActiveMQConnectionFactory activeMQConnectionFactory;

	@PostConstruct
	public void init() {
		if (this.config.startEmbeddedBroker()) {
			this.log.info("Starting embedded broker.");
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
		Map<String, Object> params = Collections.singletonMap(
				org.apache.activemq.artemis.core.remoting.impl.invm.TransportConstants.SERVER_ID_PROP_NAME, "1");
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
			this.log.info("Stopping embedded broker.");
			try {
				this.jmsServerManager.stop();
			} catch (Exception e) {
				throw new IllegalStateException("Unable to stop embedded JMS", e);
			}
		}
	}
}
