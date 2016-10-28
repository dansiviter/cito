package cito.stomp.server;

import javax.enterprise.inject.Produces;
import javax.websocket.Session;

import org.apache.logging.log4j.LogManager;

import cito.stomp.server.annotation.WebSocketScope;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [27 Oct 2016]
 */
public class SecurityContextProvider {
	/**
	 * 
	 * @param session
	 * @return
	 */
	@Produces @WebSocketScope
	public SecurityContext session(Session session) {
		LogManager.getLogger(SecurityContext.class).info("Returning SecurityContext... [sessionId={}]", session.getId());
		return (SecurityContext) session.getUserProperties().get(SecurityContext.class.getSimpleName());
	}
}
