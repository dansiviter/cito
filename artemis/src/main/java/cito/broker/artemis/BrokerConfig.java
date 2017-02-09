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

import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [2 Feb 2017]
 */
public interface BrokerConfig {

	String IN_VM_CONNECTOR = InVMConnectorFactory.class.getName();
	String REMOTE_CONNECTOR = NettyConnectorFactory.class.getName();

	/**
	 * @return if present, sends a username for the connection
	 */
	String getUsername();

	/**
	 * @return the password for the connection.  If username is set, password must be set
	 */
	String getPassword();

	/**
	 * Either url should be set, or host, port, connector factory should be set.
	 *
	 * @return if set, will be used in the server locator to look up the server instead of the hostname/port combination
	 */
	String getUrl();

	/**
	 * @return The hostname to connect to
	 */
	String getHost();

	/**
	 * @return the port number to connect to
	 */
	Integer getPort();

	/**
	 * @return the connector factory to use for connections.
	 */
	String getConnectorFactory();

	/**
	 * @return Whether or not to start the embedded broker
	 */
	boolean startEmbeddedBroker();

	/**
	 * @return whether or not this is an HA connection
	 */
	boolean isHa();

	/**
	 * @return whether or not the authentication parameters should be used
	 */
	boolean hasAuthentication();

	/**
	 * @return the configuration that will be used in the embedded broker.
	 */
	Configuration getEmbeddedConfiguration();
}
