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

import static java.util.Collections.emptyMap;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.websocket.Session;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import cito.annotation.FromServer;
import cito.event.Message;
import cito.ext.Serialiser;
import cito.io.ByteBufferOutputStream;
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
 * <p/>
 * This has asynchronous capability using CDI 2.0 asynchronous events or, if only CDI 1.2 is available it will use the
 * {@link ManagedExecutorService} for similar behaviour.
 * 
 * @author Daniel Siviter
 * @since v1.0 [27 Jul 2016]
 */
@Dependent
public class MessagingSupport {
	private static final MethodHandle FIRE_ASYNC;

	static {
		MethodHandle fireAsync = null;
		try {
			fireAsync = MethodHandles.lookup().findVirtual(
					Event.class,
					"fireAsync",
					MethodType.methodType(void.class, Object.class));
		} catch (NoSuchMethodException | IllegalAccessException e) {
			// do nothing!
		}
		FIRE_ASYNC = fireAsync;
	}

	@Resource
	private ManagedExecutorService executor;

	@Inject
	private Logger log;
	@Inject @FromServer
	private Event<Message> msgEvent;
	@Inject
	private SessionRegistry registry;
	@Inject
	private Serialiser serialiser;

	private Supplier<ByteBuffer> bufferSupplier = () -> ByteBuffer.allocate(4 * 1_024);

	/**
	 * Broadcast to all users and all sessions subscribed to the {@code destination}.
	 * 
	 * @param destination
	 * @param payload the send payload.
	 * @throws IOException if unable to serialise the payload.
	 */
	public void broadcast(
			@Nonnull String destination,
			@Nonnull Object payload)
					throws IOException
	{
		broadcast(destination, payload, (MediaType) null);
	}

	/**
	 * Broadcast to all users and all sessions subscribed to the {@code destination}.
	 * 
	 * @param destination the broadcast destination.
	 * @param payload the send payload.
	 * @param type the type to convert the data to.
	 * @throws IOException if unable to serialise the payload.
	 */
	public void broadcast(
			@Nonnull String destination,
			@Nonnull Object payload,
			@Nonnull MediaType type)
					throws IOException
	{
		broadcast(destination, payload, type, emptyMap());
	}

	/**
	 * Broadcast to all users and all sessions subscribed to the {@code destination}.
	 * 
	 * @param destination the broadcast destination.
	 * @param payload the send payload.
	 * @param headers
	 * @throws IOException if unable to serialise the payload.
	 */
	public void broadcast(
			@Nonnull String destination,
			@Nonnull Object payload,
			@Nonnull Map<Header, String> headers)
					throws IOException
	{
		broadcast(destination, payload, null, headers);
	}

	/**
	 * Broadcast to all users and all sessions subscribed to the {@code destination}.
	 * 
	 * @param destination the broadcast destination.
	 * @param payload the send payload.
	 * @param type the type to convert the data to.
	 * @param headers
	 * @throws IOException if unable to serialise the payload.
	 */
	public void broadcast(
			@Nonnull String destination,
			@Nonnull Object payload,
			@Nonnull MediaType type,
			@Nonnull Map<Header, String> headers)
	throws IOException
	{
		this.log.debug("Broadcasting... [destination={}]", destination);
		final Frame frame = Frame.send(destination, type, toByteBuffer(payload, type)).headers(headers).build();
		this.msgEvent.fire(new Message(frame));
	}

	/**
	 * Broadcast to all users and all sessions subscribed to the {@code destination}.
	 * 
	 * @param destination the broadcast destination.
	 * @param payload the send payload.
	 * @param type the type to convert the data to.
	 * @return
	 * @param headers
	 */
	public CompletionStage<Void> broadcastAsync(
			@Nonnull String destination,
			@Nonnull Object payload,
			@Nonnull MediaType type,
			@Nonnull Map<Header, String> headers)
	{
		this.log.debug("Async. broadcasting... [destination={}]", destination);
		try {
			final Frame frame = Frame.send(destination, type, toByteBuffer(payload, type)).headers(headers).build();
			return fireAsync(new Message(frame));
		} catch (IOException e) {
			return exceptionally(e);
		}
	}

	/**
	 * Broadcast to all sessions for the user defined by the {@link Principal}.
	 * 
	 * @param principal
	 * @param session
	 * @param destination the broadcast destination.
	 * @param payload the send payload.
	 * @throws IOException if unable to serialise the payload.
	 */
	public void broadcastTo(
			@Nonnull Principal principal,
			@Nonnull String destination,
			@Nonnull Object payload)
					throws IOException
	{
		broadcastTo(principal, destination, payload, emptyMap());
	}

	/**
	 * Broadcast to all sessions for the user defined by the {@link Principal}.
	 * 
	 * @param principal
	 * @param destination the broadcast destination.
	 * @param payload the send payload.
	 * @param type the type to convert the data to.
	 * @throws IOException if unable to serialise the payload.
	 */
	public void broadcastTo(
			@Nonnull Principal principal,
			@Nonnull String destination,
			@Nonnull Object payload,
			@Nonnull MediaType type)
					throws IOException
	{
		broadcastTo(principal, destination, payload, type, emptyMap());
	}

	/**
	 * Broadcast to all sessions for the user defined by the {@link Principal}.
	 * 
	 * @param principal
	 * @param destination the broadcast destination.
	 * @param payload the send payload.
	 * @param headers
	 * @throws IOException if unable to serialise the payload.
	 */
	public void broadcastTo(
			@Nonnull Principal principal,
			@Nonnull String destination,
			@Nonnull Object payload,
			@Nonnull Map<Header, String> headers)
					throws IOException
	{
		broadcastTo(principal, destination, payload, null, headers);
	}

	/**
	 * Broadcast to all sessions for the user defined by the {@link Principal}.
	 * 
	 * @param principal
	 * @param destination the broadcast destination.
	 * @param payload the send payload.
	 * @param type the type to convert the data to.
	 * @param headers
	 * @throws IOException if unable to serialise the payload.
	 */
	public void broadcastTo(
			@Nonnull Principal principal,
			@Nonnull String destination,
			@Nonnull Object payload,
			@Nonnull MediaType type,
			@Nonnull Map<Header, String> headers)
					throws IOException
	{
		// TODO inefficient to serialise the same frame each time
		for (Session s : this.registry.getSessions(principal)) {
			sendTo(s.getId(), destination, payload, type, headers);
		}
	}

	/**
	 * Broadcast to all sessions for the user defined by the {@link Principal}.
	 * 
	 * @param principal
	 * @param destination the broadcast destination.
	 * @param payload the send payload.
	 * @param type the type to convert the data to.
	 * @param headers
	 * @throws IOException if unable to serialise the payload.
	 */
	public CompletionStage<Void> broadcastToAsync(
			@Nonnull Principal principal,
			@Nonnull String destination,
			@Nonnull Object payload,
			@Nonnull MediaType type,
			@Nonnull Map<Header, String> headers)
					throws IOException
	{
		// TODO inefficient to serialise the same frame each time
		List<CompletableFuture<Void>> set = new ArrayList<>();
		for (Session s : this.registry.getSessions(principal)) {
			set.add(sendToAsync(s.getId(), destination, payload, type, headers).toCompletableFuture());
		}
		return CompletableFuture.allOf(set.toArray(new CompletableFuture[set.size()]));
	}

	/**
	 * Send to a specific user session.
	 * 
	 * @param sessionId the user session identifier to send to.
	 * @param destination
	 * @param payload the send payload.
	 * @param type the type to convert the data to.
	 * @throws IOException if unable to serialise the payload.
	 */
	public void sendTo(
			@Nonnull String sessionId,
			@Nonnull String destination,
			@Nonnull Object payload,
			@Nonnull MediaType type)
					throws IOException
	{
		sendTo(sessionId, destination, payload, type, emptyMap());
	}

	/**
	 * Send to a specific user session.
	 * 
	 * @param sessionId the user session identifier to send to.
	 * @param destination
	 * @param payload the send payload.
	 * @throws IOException if unable to serialise the payload.
	 */
	public void sendTo(
			@Nonnull String sessionId,
			@Nonnull String destination,
			@Nonnull Object payload)
					throws IOException
	{
		sendTo(sessionId, destination, payload, null, emptyMap());
	}

	/**
	 * Send to a specific user session.
	 * 
	 * @param sessionId the user session identifier to send to.
	 * @param destination
	 * @param payload the send payload.
	 * @param headers
	 * @throws IOException if unable to serialise the payload.
	 */
	public void sendTo(
			@Nonnull String sessionId,
			@Nonnull String destination,
			@Nonnull Object payload,
			@Nonnull Map<Header, String> headers)
					throws IOException
	{
		sendTo(sessionId, destination, payload, null, headers);
	}

	/**
	 * Send to a specific user session.
	 * 
	 * @param sessionId the user session identifier to send to.
	 * @param destination
	 * @param payload the send payload.
	 * @param type the type to convert the data to.
	 * @param headers
	 * @throws IOException if unable to serialise the payload.
	 */
	public void sendTo(
			@Nonnull String sessionId,
			@Nonnull String destination,
			@Nonnull Object payload,
			@Nonnull MediaType type,
			@Nonnull Map<Header, String> headers)
	throws IOException
	{
		this.log.debug("Sending... [sessionId={},destination={}]", sessionId, destination);
		final Frame frame = Frame.send(destination, type, toByteBuffer(payload, type))
				.session(sessionId)
				.headers(headers)
				.build();
		this.msgEvent.fire(new Message(frame));
	}

	/**
	 * Send to a specific user session.
	 * 
	 * @param sessionId the user session identifier to send to.
	 * @param destination
	 * @param payload the send payload.
	 * @param type the type to convert the data to.
	 * @param headers
	 * @return
	 * @throws IOException if unable to serialise the payload.
	 */
	public CompletionStage<Void> sendToAsync(
			@Nonnull String sessionId,
			@Nonnull String destination,
			@Nonnull Object payload,
			@Nonnull MediaType type,
			@Nonnull Map<Header, String> headers)
	throws IOException
	{
		this.log.debug("Sending... [sessionId={},destination={}]", sessionId, destination);
		final Frame frame = Frame.send(destination, type, toByteBuffer(payload, type))
				.session(sessionId)
				.headers(headers)
				.build();
		return fireAsync(new Message(frame));
	}

	/**
	 * 
	 * @param msg
	 * @return
	 */
	private CompletionStage<Void> fireAsync(Message msg) {
		if (FIRE_ASYNC == null) {
			return CompletableFuture.runAsync(
					() -> this.msgEvent.fire(msg),
					this.executor);
		}
		try {
			return (CompletionStage<Void>) FIRE_ASYNC.invoke(this.msgEvent, msg);
		} catch (RuntimeException e) {
			throw (RuntimeException) e;
		} catch (Throwable e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Serialises the payload.
	 * 
	 * @param obj
	 * @param type
	 * @return the object serialised to a {@link ByteBuffer}.
	 * @throws IOException if unable to serialise the payload.
	 */
	private ByteBuffer toByteBuffer(@Nonnull Object obj, @Nonnull MediaType type) throws IOException {
		final ByteBuffer buf = this.bufferSupplier.get();
		serialise(obj, type, buf);
		buf.flip();
		return buf;
	}

	/**
	 * Serialises the payload.
	 * 
	 * @param obj
	 * @param type
	 * @param buf the buffer to serialise to.
	 * @throws IOException
	 */
	private void serialise(@Nonnull Object obj, @Nonnull MediaType type, ByteBuffer buf) throws IOException {
		try (OutputStream os = new ByteBufferOutputStream(buf)) {
			this.serialiser.writeTo(obj, obj.getClass(), type, os);
		}
	}


	// --- Static Methods ---

	/**
	 * 
	 * @param ex
	 * @return
	 */
	private static CompletionStage<Void> exceptionally(Throwable ex) {
		final CompletableFuture<Void> completable = new CompletableFuture<>();
		completable.completeExceptionally(ex);
		return completable;
	}
}
