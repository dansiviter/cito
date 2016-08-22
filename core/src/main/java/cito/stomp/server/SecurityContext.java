package cito.stomp.server;

import java.security.Principal;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.websocket.Session;

import cito.stomp.server.annotation.WebSocketScope;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [17 Aug 2016]
 */
public interface SecurityContext {
	/**
	 * Returns a {@link Principal} object containing the name of the current authenticated user. If the user
	 * has not been authenticated, the method returns null.
	 *
	 * @return a {@code Principal} containing the name of the user making this request; {@code null} if the user has
	 * not been authenticated
	 * @throws java.lang.IllegalStateException if called outside the scope of a websocket session.
	 */
	Principal getUserPrincipal();

	/**
	 * Returns a boolean indicating whether the authenticated user is included in the specified logical "role". If the
	 * user has not been authenticated, the method returns {@code false}.
	 *
	 * @param role a {@code String} specifying the name of the role
	 * @return a {@code boolean} indicating whether the user making the request belongs to a given role; {@code false}
	 * if the user has not been authenticated
	 * @throws java.lang.IllegalStateException if called outside the scope of a websocket session.
	 */
	boolean isUserInRole(String role);


	// --- Static Methods ---

	/**
	 * 
	 * @param session
	 * @return
	 */
	@Produces @WebSocketScope @Inject
	public static SecurityContext session(Session session) {
		return (SecurityContext) session.getUserProperties().get(SecurityContext.class.getSimpleName());
	}
}
