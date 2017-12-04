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

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedTransferQueue;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.websocket.CloseReason;
import javax.websocket.Extension;
import javax.websocket.MessageHandler.Whole;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * @author Daniel Siviter
 * @since v1.0 [8 Jul 2017]
 */
public class WebSocketSession extends AbstractSession implements Whole<String> {
	private final Session delegate;

	private Basic basic;

	/**
	 * 
	 * @param delegate
	 */
	public WebSocketSession(Session delegate) {
		super(delegate.getContainer());
		this.delegate = delegate;
		this.delegate.addMessageHandler(this);
	}

	/**
	 * 
	 * @return
	 */
	public WebSocketSession sendOpen() {
		try {
			this.delegate.getBasicRemote().sendText("o");
		} catch (IOException e) {
			this.log.warn("Error sending.", e);
		}
		return this;
	}

	@Override
	public WebSocketContainer getContainer() {
		return this.delegate.getContainer();
	}

	@Override
	public String getProtocolVersion() {
		return this.delegate.getProtocolVersion();
	}

	@Override
	public String getNegotiatedSubprotocol() {
		return this.delegate.getNegotiatedSubprotocol();
	}

	@Override
	public List<Extension> getNegotiatedExtensions() {
		return this.delegate.getNegotiatedExtensions();
	}

	@Override
	public boolean isSecure() {
		return this.delegate.isSecure();
	}

	@Override
	public boolean isOpen() {
		return this.delegate.isOpen();
	}

	@Override
	public long getMaxIdleTimeout() {
		return this.delegate.getMaxIdleTimeout();
	}

	@Override
	public void setMaxIdleTimeout(long milliseconds) {
		this.delegate.setMaxIdleTimeout(milliseconds);
	}

	@Override
	public void setMaxBinaryMessageBufferSize(int length) {
		this.delegate.setMaxBinaryMessageBufferSize(length);
	}

	@Override
	public int getMaxBinaryMessageBufferSize() {
		return this.delegate.getMaxBinaryMessageBufferSize();
	}

	@Override
	public void setMaxTextMessageBufferSize(int length) {
		this.delegate.setMaxTextMessageBufferSize(length);
	}

	@Override
	public int getMaxTextMessageBufferSize() {
		return this.delegate.getMaxTextMessageBufferSize();
	}

	@Override
	public Basic getBasicRemote() {
		if (this.basic == null) {
			this.basic = new DefaultBasic();
		}
		return this.basic;
	}

	@Override
	public String getId() {
		return this.delegate.getId();
	}
	
	/**
	 * 
	 * @return
	 */
	private void sendClose(CloseReason reason) {
		try {
			this.delegate.getBasicRemote().sendText(
					String.format("c[%d,\"%s\"]",
							reason.getCloseCode().getCode(),
							reason.getReasonPhrase()));
		} catch (IOException e) {
			this.log.warn("Error sending.", e);
		}
	}

	@Override
	public void close() throws IOException {
		sendClose(GO_AWAY);
		this.delegate.close();
	}

	@Override
	public void close(CloseReason closeReason) throws IOException {
		sendClose(closeReason);
		this.delegate.close(closeReason);
	}

	@Override
	public URI getRequestURI() {
		return this.delegate.getRequestURI();
	}

	@Override
	public Map<String, List<String>> getRequestParameterMap() {
		return this.delegate.getRequestParameterMap();
	}

	@Override
	public String getQueryString() {
		return this.delegate.getQueryString();
	}

	@Override
	public Map<String, String> getPathParameters() {
		return this.delegate.getPathParameters();
	}

	@Override
	public Map<String, Object> getUserProperties() {
		return this.delegate.getUserProperties();
	}

	@Override
	public Principal getUserPrincipal() {
		return this.delegate.getUserPrincipal();
	}

	@Override
	public Set<Session> getOpenSessions() {
		return this.delegate.getOpenSessions();
	}

	@Override
	public void onMessage(String message) {
		this.log.info("Message recieved! [{}]", message);

		if (isEmpty(message)) {
			return; // ignore
		}
		if (message.charAt(0) != 'a') {
			throw new IllegalStateException("Unknown message type! [" + message + "]");
		}

		final String jsonArray = message.substring(1);
		if (isEmpty(message)) {
			return; // ignore
		}

		try (JsonParser parser = Json.createParser(new StringReader(jsonArray))) {
			while (parser.hasNext()) {
				final Event evt = parser.next();
				switch (evt) {
				case START_ARRAY:
					continue;
				case END_ARRAY:
					break;
				case VALUE_STRING:
					final String value = parser.getString();
					forwardMessage(StringEscapeUtils.unescapeJson(value));
					continue;
				default:
					throw new JsonException("Only Array Start/End and String expected!");
				}
			}
		} catch (IOException | JsonException e) {
			final String error = e instanceof JsonException ? "Broken JSON encoding." : "Error processing data!";
			this.log.warn(error, e);
		}
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [29 Jul 2016]
	 */
	public class DefaultBasic extends AbstractBasic {
		private final LinkedTransferQueue<String> frameQueue = new LinkedTransferQueue<>();
		private final StringBuilder buf = new StringBuilder();

		@Override
		public void sendText(String msg) throws IOException {
			log.info("Sending message. [sessionId={},msg={}]", getId(), msg);
			frameQueue.add(msg);
			flush();
		}

		@Override
		public void sendText(String msg, boolean last) throws IOException {
			log.info("Sending message. [sessionId={},msg={},last={}]", getId(), msg, last);
			synchronized (this.buf) {
				this.buf.append(msg);
				if (last) {
					frameQueue.add(buf.toString());
					this.buf.setLength(0);
					this.buf.trimToSize();
					flush();
				}
			}
		}
	
		private void flush() throws IOException {
			final Basic delegate = WebSocketSession.this.delegate.getBasicRemote();
			delegate.sendText("a[\"", false);
			while (!frameQueue.isEmpty()) {
				final String frame = frameQueue.poll();
				log.debug("Flushing frame. [sessionId={},frame={}]", getId(), frame);
				delegate.sendText(StringEscapeUtils.escapeJson(frame), false);
				if (!frameQueue.isEmpty()) {
					delegate.sendText("\",\"", false);
				}
			}
			delegate.sendText("\"]", true);
		}
	}
}
