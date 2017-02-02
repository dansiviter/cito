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
