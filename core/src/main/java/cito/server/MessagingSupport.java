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

import static cito.annotation.Qualifiers.fromServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import cito.event.MessageEvent;
import cito.ext.Serialiser;
import cito.stomp.Frame;

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
@Dependent
public class MessagingSupport {
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
		this.msgEvent.select(fromServer()).fire(new MessageEvent(frame));
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
		this.msgEvent.select(fromServer()).fire(new MessageEvent(frame));
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
}
