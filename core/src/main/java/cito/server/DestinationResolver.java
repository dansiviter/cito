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
package cito.server;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.Session;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [4 Aug 2016]
 */
@ApplicationScoped
public class DestinationResolver {
	@Inject
	private SessionRegistry registry;

	/**
	 * 
	 * @param principal
	 * @param destination
	 * @return
	 */
	public Set<String> resolve(Principal principal, String destination) {
		final Set<Session> sessions = registry.getSessions(principal);
		final Set<String> destinations = new HashSet<>(sessions.size());
		for (Session session : sessions) {
			destinations.add(resolve(session.getId(), destination));
		}
		return destinations;
	}

	/**
	 * 
	 * @param sessionId
	 * @param destination
	 * @return
	 */
	public String resolve(String sessionId, String destination) {
		return destination.concat("-").concat(sessionId);
	}
}
