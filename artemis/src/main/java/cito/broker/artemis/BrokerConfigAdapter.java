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
