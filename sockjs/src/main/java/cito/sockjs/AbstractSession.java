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

import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.CloseReason;
import javax.websocket.Extension;
import javax.websocket.MessageHandler;
import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [9 Aug 2016]
 */
public abstract class AbstractSession implements Session {
	private final HttpServletRequest request;
	private final Map<String, Object> userProperties;
	private final Map<String, List<String>> paramMap;

	public AbstractSession(HttpServletRequest request, Map<String, Object> userProperties) {
		this.request = request;
		this.userProperties = new ConcurrentHashMap<>(userProperties);

		final Map<String, List<String>> paramMap = new HashMap<>();
		for (Entry<String, String[]> e : request.getParameterMap().entrySet()) {
			paramMap.put(e.getKey(), Collections.unmodifiableList(Arrays.asList(e.getValue())));
		}
		this.paramMap = Collections.unmodifiableMap(paramMap);
	}

	@Override
	public WebSocketContainer getContainer() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addMessageHandler(MessageHandler handler) throws IllegalStateException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<MessageHandler> getMessageHandlers() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeMessageHandler(MessageHandler handler) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getProtocolVersion() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getNegotiatedSubprotocol() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Extension> getNegotiatedExtensions() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSecure() {
		return this.request.isSecure();
	}

	@Override
	public boolean isOpen() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getMaxIdleTimeout() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setMaxIdleTimeout(long milliseconds) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setMaxBinaryMessageBufferSize(int length) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getMaxBinaryMessageBufferSize() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setMaxTextMessageBufferSize(int length) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getMaxTextMessageBufferSize() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Async getAsyncRemote() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Basic getBasicRemote() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close(CloseReason closeReason) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public URI getRequestURI() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, List<String>> getRequestParameterMap() {
		return this.paramMap;
	}

	@Override
	public String getQueryString() {
		return this.request.getQueryString();
	}

	@Override
	public Map<String, String> getPathParameters() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, Object> getUserProperties() {
		return this.userProperties;
	}

	@Override
	public Principal getUserPrincipal() {
		return this.request.getUserPrincipal();
	}

	@Override
	public Set<Session> getOpenSessions() {
		throw new UnsupportedOperationException();
	}
}
