package flngr.stomp.server;

import static flngr.stomp.server.annotation.Qualifiers.fromClient;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import flngr.stomp.Frame;
import flngr.stomp.server.event.Message;

/**
 * Either extend or inject this class where you wish to use it.
 * 
 * @author Daniel Siviter
 * @since v1.0 [27 Jul 2016]
 */
public abstract class Support {
	@Inject
	private Event<Message> msgEvent;
	@Inject
	private SessionRegistry registry;

	/**
	 * Broadcast to all users and all sessions subscribed to the {@code destination}.
	 * 
	 * @param destination
	 * @param payload
	 */
	public void broadcast(String destination, Object payload) {
		broadcast(destination, null, payload);
	}

	/**
	 * Broadcast to all users and all sessions subscribed to the {@code destination}.
	 * 
	 * @param destination
	 * @param type
	 * @param payload
	 */
	public void broadcast(String destination, MediaType type, Object payload) {
		broadcast(destination, payload, Collections.<String, String>emptyMap());
	}

	/**
	 * Broadcast to all users and all sessions subscribed to the {@code destination}.
	 * 
	 * @param destination
	 * @param payload
	 * @param headers
	 */
	public void broadcast(String destination, Object payload, Map<String, String> headers) {
		broadcast(destination, null, payload, headers);
	}

	/**
	 * Broadcast to all users and all sessions subscribed to the {@code destination}.
	 * 
	 * @param destination
	 * @param type
	 * @param payload
	 * @param headers
	 */
	public void broadcast(String destination, MediaType type, Object payload, Map<String, String> headers) {
		if (type == null) type = MediaType.APPLICATION_JSON_TYPE;
		final Frame frame = Frame.send(destination, type, payload.toString()).headers(headers).build();
		this.msgEvent.select(fromClient()).fire(new Message(frame));
	}

	/**
	 * Broadcast to all sessions for the user defined by the {@link Principal}.
	 * 
	 * @param principal
	 * @param session
	 * @param destination
	 * @param payload
	 */
	public void broadcastTo(Principal principal, String destination, Object payload) {
		broadcastTo(principal, destination, payload, Collections.<String, String>emptyMap());
	}

	/**
	 * Broadcast to all sessions for the user defined by the {@link Principal}.
	 * 
	 * @param principal
	 * @param destination
	 * @param type
	 * @param payload
	 */
	public void broadcastTo(Principal principal, String destination, MediaType type, Object payload) {
		broadcastTo(principal, destination, type, payload, Collections.<String, String>emptyMap());
	}

	/**
	 * Broadcast to all sessions for the user defined by the {@link Principal}.
	 * 
	 * @param principal
	 * @param destination
	 * @param payload
	 * @param headers
	 */
	public void broadcastTo(Principal principal, String destination, Object payload, Map<String, String> headers) {
		broadcastTo(principal, destination, null, payload, headers);
	}

	/**
	 * Broadcast to all sessions for the user defined by the {@link Principal}.
	 * 
	 * @param principal
	 * @param destination
	 * @param type
	 * @param payload
	 * @param headers
	 */
	public void broadcastTo(Principal principal, String destination, MediaType type, Object payload, Map<String, String> headers) {
		this.registry.getSessions(principal).forEach(s -> sendTo(s.getId(), destination, type, payload, headers));
	}

	/**
	 * 
	 * @param sessionId
	 * @param destination
	 * @param type
	 * @param payload
	 */
	public void sendTo(String sessionId, String destination, MediaType type, Object payload) {
		sendTo(sessionId, destination, type, payload, Collections.<String, String>emptyMap());
	}

	/**
	 * 
	 * @param sessionId
	 * @param destination
	 * @param type
	 * @param payload
	 * @param headers
	 */
	public void sendTo(String sessionId, String destination, MediaType type, Object payload, Map<String, String> headers) {
		if (type == null) type = MediaType.APPLICATION_JSON_TYPE;
		final Frame frame = Frame.send(destination, type, payload.toString()).session(sessionId).headers(headers).build();
		this.msgEvent.select(fromClient()).fire(new Message(frame));
	}


	// --- Static Methods ---

	/**
	 * 
	 * @return
	 */
	@Produces @Dependent
	public static Support support() {
		return new Support() { };
	}
}
