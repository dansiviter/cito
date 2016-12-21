package cito.stomp.server;

import static cito.stomp.server.annotation.Qualifiers.fromServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import cito.stomp.Frame;
import cito.stomp.ext.Serialiser;
import cito.stomp.server.event.BasicMessageEvent;
import cito.stomp.server.event.MessageEvent;

/**
 * Server messaging support. This can be used in two ways: {@link Inject}ed or {@code extend} it.
 * <p/>
 * To inject, use:
 * <pre>
 * 	&#064;Inject
 * 	private MessagingSupport support;
 * </pre>
 * 
 * @author Daniel Siviter
 * @since v1.0 [27 Jul 2016]
 */
public abstract class Support {
	@Inject
	private Logger log;
	@Inject
	private Event<MessageEvent> msgEvent;
	@Inject
	private SessionRegistry registry;
	@Inject
	private Serialiser serialiser;

	/**
	 * Broadcast to all users and all sessions subscribed to the {@code destination}.
	 * 
	 * @param destination
	 * @param payload the send payload.
	 */
	public void broadcast(String destination, Object payload) {
		broadcast(destination, null, payload);
	}

	/**
	 * Broadcast to all users and all sessions subscribed to the {@code destination}.
	 * 
	 * @param destination the broadcast destination.
	 * @param type if {@code null} defaults to {@code application/json}.
	 * @param payload the send payload.
	 */
	public void broadcast(String destination, MediaType type, Object payload) {
		broadcast(destination, payload, Collections.<String, String>emptyMap());
	}

	/**
	 * Broadcast to all users and all sessions subscribed to the {@code destination}.
	 * 
	 * @param destination the broadcast destination.
	 * @param payload the send payload.
	 * @param headers
	 */
	public void broadcast(String destination, Object payload, Map<String, String> headers) {
		broadcast(destination, null, payload, headers);
	}

	/**
	 * Broadcast to all users and all sessions subscribed to the {@code destination}.
	 * 
	 * @param destination the broadcast destination.
	 * @param type if {@code null} defaults to {@code application/json}.
	 * @param payload the send payload.
	 * @param headers
	 */
	public void broadcast(String destination, MediaType type, Object payload, Map<String, String> headers) {
		if (type == null) type = MediaType.APPLICATION_JSON_TYPE;
		this.log.debug("Broadcasting... [destination={}]", destination);
		final Frame frame = Frame.send(destination, type, toByteBuffer(payload, type)).headers(headers).build();
		this.msgEvent.select(fromServer()).fire(new BasicMessageEvent(frame));
	}

	/**
	 * Broadcast to all sessions for the user defined by the {@link Principal}.
	 * 
	 * @param principal
	 * @param session
	 * @param destination the broadcast destination.
	 * @param payload the send payload.
	 */
	public void broadcastTo(Principal principal, String destination, Object payload) {
		broadcastTo(principal, destination, payload, Collections.<String, String>emptyMap());
	}

	/**
	 * Broadcast to all sessions for the user defined by the {@link Principal}.
	 * 
	 * @param principal
	 * @param destination the broadcast destination.
	 * @param type if {@code null} defaults to {@code application/json}.
	 * @param payload the send payload.
	 */
	public void broadcastTo(Principal principal, String destination, MediaType type, Object payload) {
		broadcastTo(principal, destination, type, payload, Collections.<String, String>emptyMap());
	}

	/**
	 * Broadcast to all sessions for the user defined by the {@link Principal}.
	 * 
	 * @param principal
	 * @param destination the broadcast destination.
	 * @param payload the send payload.
	 * @param headers
	 */
	public void broadcastTo(Principal principal, String destination, Object payload, Map<String, String> headers) {
		broadcastTo(principal, destination, null, payload, headers);
	}

	/**
	 * Broadcast to all sessions for the user defined by the {@link Principal}.
	 * 
	 * @param principal
	 * @param destination the broadcast destination.
	 * @param type if {@code null} defaults to {@code application/json}.
	 * @param payload the send payload.
	 * @param headers
	 */
	public void broadcastTo(Principal principal, String destination, MediaType type, Object payload, Map<String, String> headers) {
		this.registry.getSessions(principal).forEach(s -> sendTo(s.getId(), destination, type, payload, headers));
	}

	/**
	 * Send to a specific user session.
	 * 
	 * @param sessionId the user session identifier to send to.
	 * @param destination
	 * @param type if {@code null} defaults to {@code application/json}.
	 * @param payload the send payload.
	 */
	public void sendTo(String sessionId, String destination, MediaType type, Object payload) {
		sendTo(sessionId, destination, type, payload, Collections.<String, String>emptyMap());
	}

	/**
	 * Send to a specific user session.
	 * 
	 * @param sessionId the user session identifier to send to.
	 * @param destination
	 * @param payload the send payload.
	 */
	public void sendTo(String sessionId, String destination, Object payload) {
		sendTo(sessionId, destination, null, payload, Collections.<String, String>emptyMap());
	}

	/**
	 * Send to a specific user session.
	 * 
	 * @param sessionId the user session identifier to send to.
	 * @param destination
	 * @param payload the send payload.
	 * @param headers
	 */
	public void sendTo(String sessionId, String destination, Object payload, Map<String, String> headers) {
		sendTo(sessionId, destination, null, payload, headers);
	}

	/**
	 * Send to a specific user session.
	 * 
	 * @param sessionId the user session identifier to send to.
	 * @param destination
	 * @param type if {@code null} defaults to {@code application/json}.
	 * @param payload the send payload.
	 * @param headers
	 */
	public void sendTo(String sessionId, String destination, MediaType type, Object payload, Map<String, String> headers) {
		if (type == null) type = MediaType.APPLICATION_JSON_TYPE;
		this.log.debug("Sending... [sessionId={},destination={}]", sessionId, destination);
		final Frame frame = Frame.send(destination, type, toByteBuffer(payload, type)).session(sessionId).headers(headers).build();
		this.msgEvent.select(fromServer()).fire(new BasicMessageEvent(frame));
	}

	/**
	 * 
	 * @param obj
	 * @param type
	 * @return the object as a {@link ByteBuffer} or {@code null} if {@code obj} was {@code null}.
	 */
	private ByteBuffer toByteBuffer(Object obj, MediaType type) {
		if (obj == null) {
			return null;
		}
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			this.serialiser.writeTo(obj, obj.getClass(), type, os);
			return ByteBuffer.wrap(os.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	// --- Static Methods ---

	/**
	 * @return an instance of Support for {@link Inject} use-case.
	 */
	@Produces @Dependent
	public static MessagingSupport support() {
		return new MessagingSupport();
	}


	// --- Inner Classes ---

	/**
	 * Concrete version for injection.
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [9 Nov 2016]
	 */
	public static class MessagingSupport extends Support { 
		MessagingSupport() { }
	}
}
