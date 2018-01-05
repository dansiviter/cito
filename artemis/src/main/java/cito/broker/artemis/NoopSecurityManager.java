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

import java.util.Set;

import org.apache.activemq.artemis.core.security.CheckType;
import org.apache.activemq.artemis.core.security.Role;
import org.apache.activemq.artemis.spi.core.protocol.RemotingConnection;
import org.apache.activemq.artemis.spi.core.security.ActiveMQSecurityManager3;

/**
 * Defines a no-op version of {@link ActiveMQSecurityManager3}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [15 Aug 2017]
 */
public class NoopSecurityManager implements ActiveMQSecurityManager3 {
	public static final ActiveMQSecurityManager3 INSTANCE = new NoopSecurityManager();

	@Override
	public boolean validateUser(String user, String password) {
		return true;
	}

	@Override
	public boolean validateUserAndRole(String user, String password, Set<Role> roles, CheckType checkType) {
		return true;
	}

	@Override
	public String validateUser(String user, String password, RemotingConnection remotingConnection) {
		return user == null || user.isEmpty() ? "UNKNOWN" : user;
	}

	@Override
	public String validateUserAndRole(String user, String password, Set<Role> roles, CheckType checkType,
			String address, RemotingConnection remotingConnection) {
		return user == null || user.isEmpty() ? "UNKNOWN" : user;
	}
}
