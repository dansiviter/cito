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

import javax.enterprise.inject.Vetoed;

import org.apache.activemq.artemis.api.core.SimpleString;
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
//				.setJMXManagementEnabled(false)
				.setManagementNotificationAddress(new SimpleString("jms.topic.notifications"))
				.addAcceptorConfiguration(new TransportConfiguration(InVMAcceptorFactory.class.getName(), params));
	}
}