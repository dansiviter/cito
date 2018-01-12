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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import cito.annotation.FromServer;
import cito.event.Message;
import cito.ext.Serialiser;
import cito.stomp.Frame;
import cito.stomp.Header;

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
	private Event<Message> msgEvent;
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
	public void broadcast(String destination, @Nonnull Object payload) {
		broadcast(destination, payload, (MediaType) null);
	}

	/**
	 * Broadcast to all users and all sessions subscribed to the {@code destination}.
	 * 
	 * @param destination the broadcast destination.
	 * @param payload the send payload.
	 * @param type if {@code null} defaults to {@code application/json}.
	 */
	public void broadcast(String destination, @Nonnull Object payload, MediaType type) {
		broadcast(destination, payload, type, Collections.<Header, String>emptyMap());
	}

	/**
	 * Broadcast to all users and all sessions subscribed to the {@code destination}.
	 * 
	 * @param destination the broadcast destination.
	 * @param payload the send payload.
	 * @param headers
	 */
	public void broadcast(String destination, @Nonnull Object payload, Map<Header, String> headers) {
		broadcast(destination, payload, null, headers);
	}

	/**
	 * Broadcast to all users and all sessions subscribed to the {@code destination}.
	 * 
	 * @param destination the broadcast destination.
	 * @param payload the send payload.
	 * @param type if {@code null} defaults to {@code application/json}.
	 * @param headers
	 */
	public void broadcast(String destination, @Nonnull Object payload, MediaType type, Map<Header, String> headers) {
		if (type == null) {
			type = MediaType.APPLICATION_JSON_TYPE;
		}
		this.log.debug("Broadcasting... [destination={}]", destination);
		try {
			final Frame frame = Frame.send(destination, type, toByteBuffer(payload, type)).headers(headers).build();
			this.msgEvent.select(FromServer.Literal.fromServer()).fire(new Message(frame));
		} catch (IOException e) {
			this.log.warn("Unable to broadcast message! [destination=" + destination + "]", e);
		}
	}

	/**
	 * Broadcast to all sessions for the user defined by the {@link Principal}.
	 * 
	 * @param principal
	 * @param session
	 * @param destination the broadcast destination.
	 * @param payload the send payload.
	 */
	public void broadcastTo(@Nonnull Principal principal, String destination, @Nonnull Object payload) {
		broadcastTo(principal, destination, payload, Collections.<Header, String>emptyMap());
	}

	/**
	 * Broadcast to all sessions for the user defined by the {@link Principal}.
	 * 
	 * @param principal
	 * @param destination the broadcast destination.
	 * @param payload the send payload.
	 * @param type if {@code null} defaults to {@code application/json}.
	 */
	public void broadcastTo(@Nonnull Principal principal, String destination, @Nonnull Object payload, MediaType type) {
		broadcastTo(principal, destination, type, payload, Collections.<Header, String>emptyMap());
	}

	/**
	 * Broadcast to all sessions for the user defined by the {@link Principal}.
	 * 
	 * @param principal
	 * @param destination the broadcast destination.
	 * @param payload the send payload.
	 * @param headers
	 */
	public void broadcastTo(
			@Nonnull Principal principal,
			String destination,
			@Nonnull Object payload,
			Map<Header, String> headers)
	{
		broadcastTo(principal, destination, null, payload, headers);
	}

	/**
	 * Broadcast to all sessions for the user defined by the {@link Principal}.
	 * 
	 * @param principal
	 * @param destination the broadcast destination.
	 * @param payload the send payload.
	 * @param type if {@code null} defaults to {@code application/json}.
	 * @param headers
	 */
	public void broadcastTo(
			@Nonnull Principal principal,
			String destination,
			MediaType type,
			@Nonnull Object payload,
			Map<Header, String> headers)
	{
		this.registry.getSessions(principal).forEach(s -> sendTo(s.getId(), destination, payload, type, headers));
	}

	/**
	 * Send to a specific user session.
	 * 
	 * @param sessionId the user session identifier to send to.
	 * @param destination
	 * @param payload the send payload.
	 * @param type if {@code null} defaults to {@code application/json}.
	 */
	public void sendTo(@Nonnull String sessionId, String destination, @Nonnull Object payload, MediaType type) {
		sendTo(sessionId, destination, payload, type, Collections.<Header, String>emptyMap());
	}

	/**
	 * Send to a specific user session.
	 * 
	 * @param sessionId the user session identifier to send to.
	 * @param destination
	 * @param payload the send payload.
	 */
	public void sendTo(@Nonnull String sessionId, String destination, @Nonnull Object payload) {
		sendTo(sessionId, destination, payload, null, Collections.<Header, String>emptyMap());
	}

	/**
	 * Send to a specific user session.
	 * 
	 * @param sessionId the user session identifier to send to.
	 * @param destination
	 * @param payload the send payload.
	 * @param headers
	 */
	public void sendTo(
			@Nonnull String sessionId,
			String destination,
			@Nonnull Object payload,
			Map<Header, String> headers)
	{
		sendTo(sessionId, destination, payload, null, headers);
	}

	/**
	 * Send to a specific user session.
	 * 
	 * @param sessionId the user session identifier to send to.
	 * @param destination
	 * @param payload the send payload.
	 * @param type if {@code null} defaults to {@code application/json}.
	 * @param headers
	 */
	public void sendTo(
			@Nonnull String sessionId,
			String destination,
			@Nonnull Object payload,
			MediaType type,
			Map<Header, String> headers)
	{
		if (type == null) {
			type = MediaType.APPLICATION_JSON_TYPE;
		}
		this.log.debug("Sending... [sessionId={},destination={}]", sessionId, destination);
		try {
			final Frame frame = Frame.send(destination, type, toByteBuffer(payload, type)).session(sessionId).headers(headers).build();
			this.msgEvent.select(FromServer.Literal.fromServer()).fire(new Message(frame));
		} catch (IOException e) {
			this.log.warn("Unable to send message! [sessionId=" + sessionId + ",destination=" + destination + "]", e);
		}
	}

	/**
	 * 
	 * @param obj
	 * @param type
	 * @return the object as a {@link ByteBuffer} or {@code null} if {@code obj} was {@code null}.
	 * @throws IOException
	 */
	private ByteBuffer toByteBuffer(Object obj, MediaType type) throws IOException {
		if (obj == null) {
			return null;
		}
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			this.serialiser.writeTo(obj, obj.getClass(), type, os);
			return ByteBuffer.wrap(os.toByteArray());
		}
	}
}
