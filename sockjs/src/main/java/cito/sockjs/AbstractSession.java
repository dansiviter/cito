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
package cito.sockjs;

import static javax.websocket.CloseReason.CloseCodes.getCloseCode;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.websocket.CloseReason;
import javax.websocket.Extension;
import javax.websocket.MessageHandler;
import javax.websocket.MessageHandler.Partial;
import javax.websocket.MessageHandler.Whole;
import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines an abstract instance of WebSocket {@link Session}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [3 Jan 2017]
 */
public abstract class AbstractSession implements Session {
	protected static final CloseReason GO_AWAY = new CloseReason(getCloseCode(3000), "Go away!");

	private final Set<MessageHandlerWrapper> messageHandlers = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private final Map<String, Object> userProps = new ConcurrentHashMap<>();
	protected final Logger log;
	protected WebSocketContainer container;

	private OptionalLong maxIdleTimeout = OptionalLong.empty();
	private OptionalInt maxTextMessageBufferSize = OptionalInt.empty();
	private OptionalInt maxBinaryMessageBufferSize = OptionalInt.empty();

	/**
	 * 
	 * @param container
	 */
	public AbstractSession(WebSocketContainer container) {
		this.log = LoggerFactory.getLogger(getClass());
		this.container = container;
	}

	/**
	 * 
	 * @param session
	 * @param msg
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	public void forwardMessage(String msg) throws IOException {
		this.log.info("Forwarding message to handlers. [{}]", msg);

		try (Reader reader = new StringReader(msg)) {
			for (MessageHandlerWrapper h : getMessageHandlerWrappers()) {
				if (h.handler() instanceof MessageHandler.Whole) {
					if (h.clazz == String.class) {
						((MessageHandler.Whole<String>) h.handler()).onMessage(msg);
						continue;
					}
	
					if (h.clazz == Reader.class) {
						((MessageHandler.Whole<Reader>) h.handler()).onMessage(reader);
						reader.reset();
						continue;
					}
	
					if (h.clazz == ByteBuffer.class || h.clazz == InputStream.class) {
						this.log.warn("Binary types not supported! [{}]", h.getClass());
						continue;
					}
	
					this.log.warn("Decoder types not supported yet! [{}]", h.getClass());
				} else if (h.handler() instanceof MessageHandler.Partial) {
					this.log.warn("Partial types not supported yet! [{}]", h.getClass());
				}
			}
		}
	}
	
	@Override
	public WebSocketContainer getContainer() {
		return this.container;
	}

	@Override
	public void addMessageHandler(MessageHandler handler) throws IllegalStateException {
		this.messageHandlers.add(new MessageHandlerWrapper(handler, null));
	}

	@Override
	public <T> void addMessageHandler(Class<T> clazz, Partial<T> handler) {
		throw new UnsupportedOperationException("Partial not supported!");
	}

	@Override
	public <T> void addMessageHandler(Class<T> clazz, Whole<T> handler) {
		this.messageHandlers.add(new MessageHandlerWrapper(handler, clazz));
	}

	Set<MessageHandlerWrapper> getMessageHandlerWrappers() {
		return messageHandlers;
	}

	@Override
	public Set<MessageHandler> getMessageHandlers() {
		return getMessageHandlerWrappers().stream().map(MessageHandlerWrapper::handler).collect(Collectors.toSet());
	}

	@Override
	public void removeMessageHandler(MessageHandler handler) {
		if (!this.messageHandlers.removeIf(h -> h.handler == handler)) {
			throw new IllegalArgumentException("Handler was unknown to the session!");
		}
	}

	@Override
	public String getProtocolVersion() {
		return null;
	}

	@Override
	public String getNegotiatedSubprotocol() {
		return null;
	}

	@Override
	public List<Extension> getNegotiatedExtensions() {
		return null;
	}

	@Override
	public long getMaxIdleTimeout() {
		return this.maxIdleTimeout.orElseGet(getContainer() :: getDefaultMaxSessionIdleTimeout);
	}

	@Override
	public void setMaxIdleTimeout(long milliseconds) {
		this.maxIdleTimeout = OptionalLong.of(milliseconds);
	}

	@Override
	public int getMaxBinaryMessageBufferSize() {
		return this.maxBinaryMessageBufferSize.orElseGet(getContainer() :: getDefaultMaxBinaryMessageBufferSize);
	}

	@Override
	public void setMaxBinaryMessageBufferSize(int length) {
		this.maxBinaryMessageBufferSize = length > 0 ? OptionalInt.of(length) : OptionalInt.empty();
	}

	@Override
	public int getMaxTextMessageBufferSize() {
		return this.maxTextMessageBufferSize.orElseGet(getContainer() :: getDefaultMaxTextMessageBufferSize);
	}


	@Override
	public void setMaxTextMessageBufferSize(int length) {
		this.maxTextMessageBufferSize = length > 0 ? OptionalInt.of(length) : OptionalInt.empty();
	}

	
	@Override
	public Async getAsyncRemote() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, Object> getUserProperties() {
		return this.userProps;
	}

	@Override
	public Set<Session> getOpenSessions() {
		throw new UnsupportedOperationException();
	}


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [14 Feb 2017]
	 */
	public static class MessageHandlerWrapper {
		public final MessageHandler handler;
		public final Class<?> clazz;

		private MessageHandlerWrapper(MessageHandler handler, Class<?> clazz) {
			this.handler = handler;
			this.clazz = clazz;
		}

		/**
		 * @return the handler
		 */
		public MessageHandler handler() {
			return handler;
		}
	}
}
