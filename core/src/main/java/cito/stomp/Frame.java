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
package cito.stomp;

import static cito.stomp.Header.Standard.ACCEPT_VERSION;
import static cito.stomp.Header.Standard.CONTENT_LENGTH;
import static cito.stomp.Header.Standard.CONTENT_TYPE;
import static cito.stomp.Header.Standard.DESTINATION;
import static cito.stomp.Header.Standard.HOST;
import static cito.stomp.Header.Standard.ID;
import static cito.stomp.Header.Standard.MESSAGE_ID;
import static cito.stomp.Header.Standard.RECEIPT;
import static cito.stomp.Header.Standard.RECEIPT_ID;
import static cito.stomp.Header.Standard.SERVER;
import static cito.stomp.Header.Standard.SESSION;
import static cito.stomp.Header.Standard.SUBSCRIPTION;
import static cito.stomp.Header.Standard.TRANSACTION;
import static cito.stomp.Header.Standard.VERSION;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;

import cito.stomp.Header.Standard;

/**
 * Defines a STOMP frame
 * 
 * @author Daniel Siviter
 * @since v1.0 [12 Jul 2016]
 */
@Immutable
public class Frame {
	private static final AtomicLong MESSAGE_ID_COUNTER = new AtomicLong();
	public static final Frame HEART_BEAT = new Frame(Command.HEARTBEAT, new HashMap<>(0), null);

	private final Command command;
	private final Map<Header, List<String>> headers;
	private final ByteBuffer body;

	/**
	 * 
	 * @param command
	 * @param headers
	 */
	Frame(@Nonnull Command command, @Nonnull Map<Header, List<String>> headers) {
		this(command, headers, null);
	}

	/**
	 * 
	 * @param command
	 * @param headers
	 * @param body
	 */
	Frame(@Nonnull Command command, @Nonnull Map<Header, List<String>> headers, ByteBuffer body) {
		this.command = requireNonNull(command);
		final Map<Header, List<String>> tmpHeaders = new LinkedHashMap<>(headers);
		tmpHeaders.entrySet().forEach(e -> e.setValue(unmodifiableList(e.getValue())));
		this.headers = unmodifiableMap(tmpHeaders);
		this.body = body != null ? body.asReadOnlyBuffer() : null;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isHeartBeat() {
		return this.command == Command.HEARTBEAT;
	}

	/**
	 * 
	 * @return
	 */
	public Command getCommand() {
		return command;
	}

	/**
	 * 
	 * @return
	 */
	public Map<Header, List<String>> getHeaders() {
		return headers;
	}

	/**
	 * 
	 * @return
	 */
	public ByteBuffer getBody() {
		return body;
	}

	/**
	 * 
	 * @param header
	 * @return
	 */
	public boolean contains(Header header) {
		return get(header) != null;
	}

	/**
	 * 
	 * @param header
	 * @return
	 */
	public List<String> get(Header header) {
		return getHeaders().get(header);
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public List<String> getHeader(String key) {
		return get(Header.valueOf(key));
	}

	/**
	 * 
	 * @param header
	 * @return
	 */
	public String getFirst(@Nonnull Header header) {
		final List<String> values = get(header);
		return values != null && values.size() > 0 ? values.get(0) : null;
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public String getFirstHeader(@Nonnull String key) {
		return getFirst(Header.valueOf(key));
	}

	/**
	 * 
	 * @return
	 */
	public String destination() {
		return getFirst(DESTINATION);
	}

	/**
	 * 
	 * @return
	 */
	public int contentLength() {
		final String contentLength = getFirst(CONTENT_LENGTH);
		return contentLength != null ? Integer.parseInt(contentLength) : -1;
	}

	/**
	 * 
	 * @return
	 */
	public MediaType contentType() {
		final String contentType = getFirst(CONTENT_TYPE);
		return contentType != null ? MediaType.valueOf(contentType) : null;
	}

	/**
	 * 
	 * @return
	 */
	public int receipt() {
		return Integer.parseInt(getFirst(RECEIPT));
	}

	/**
	 * 
	 * @return
	 */
	public int receiptId() {
		return Integer.parseInt(getFirst(RECEIPT_ID));
	}

	/**
	 * 
	 * @return
	 */
	public String subscription() {
		if (this.command == Command.MESSAGE) { // why is MESSAGE so special?!
			return getFirst(SUBSCRIPTION);
		}
		return getFirst(ID);
	}

	/**
	 * 
	 * @return
	 */
	public HeartBeat heartBeat() {
		final String heartBeat = getFirst(Standard.HEART_BEAT);
		return heartBeat != null ? new HeartBeat(heartBeat) : null;
	}

	/**
	 * 
	 * @return
	 */
	public String transaction() {
		return getFirst(TRANSACTION);
	}

	/**
	 * 
	 * @return
	 */
	public String session() {
		return getFirst(SESSION);
	}

	/**
	 * Only use this for debugging purposes. An especially large frame could lead to {@link BufferOverflowException}.
	 */
	@Override
	public String toString() {
		// 64k is generally much larger than the buffer used by WebSocket implementation, so this should suffice
		return UTF_8.decode(Encoding.from(this, false, 64 * 1024)).toString();
	}


	// --- Static Methods ---

	/**
	 * 
	 * @param host
	 * @param acceptVersion
	 * @return
	 */
	public static Builder connect(@Nonnull String host, @Nonnull String... acceptVersion) {
		return builder(Command.CONNECT).header(HOST, host).header(ACCEPT_VERSION, acceptVersion);
	}

	/**
	 * 
	 * @return
	 */
	public static Builder disconnect() {
		return builder(Command.DISCONNECT);
	}

	/**
	 * 
	 * @return
	 */
	public static Builder error() {
		return builder(Command.ERROR);
	}

	/**
	 * 
	 * @param destination
	 * @param subscriptionId
	 * @param messageId
	 * @param contentType
	 * @param body
	 * @return
	 */
	public static Builder message(
			@Nonnull String destination,
			@Nonnull String subscriptionId,
			@Nonnull String messageId,
			MediaType contentType,
			@Nonnull String body)
	{
		return builder(Command.MESSAGE)
				.destination(destination)
				.subscription(subscriptionId)
				.messageId(messageId)
				.body(contentType, body);
	}

	/**
	 * 
	 * @param destination
	 * @param contentType
	 * @param body
	 * @return
	 */
	public static Builder send(@Nonnull String destination, MediaType contentType, @Nonnull ByteBuffer body) {
		return builder(Command.SEND).destination(destination).body(contentType, body);
	}

	/**
	 * 
	 * @param destination
	 * @param contentType
	 * @param body
	 * @return
	 */
	public static Builder send(@Nonnull String destination, MediaType contentType, @Nonnull String body) {
		return builder(Command.SEND).destination(destination).body(contentType, body);
	}

	/**
	 * 
	 * @param version
	 * @param session
	 * @param server
	 * @param heartBeat
	 * @return
	 */
	public static Builder connnected(@Nonnull String version, @Nonnull String session, @Nonnull String server) {
		final Builder builder = builder(Command.CONNECTED).header(VERSION, version);
		builder.header(SESSION, requireNonNull(session));
		builder.header(SERVER, requireNonNull(server));
		return builder;
	}

	/**
	 * 
	 * @param id
	 * @param destination
	 * @return
	 */
	public static Builder subscribe(@Nonnull String id, @Nonnull String destination) {
		return builder(Command.SUBSCRIBE).subscription(id).destination(destination);
	}

	/**
	 * 
	 * @param receiptId
	 * @return
	 */
	public static Builder receipt(@Nonnull String receiptId) {
		return builder(Command.RECEIPT).header(RECEIPT_ID, receiptId);
	}

	/**
	 * 
	 * @param command
	 * @return
	 */
	public static Builder builder(@Nonnull Command command) {
		return new Builder(command);
	}

	/**
	 * 
	 * @param frame
	 * @return
	 */
	public static Builder builder(@Nonnull Builder builder) {
		return new Builder(builder);
	}

	/**
	 * 
	 * @param frame
	 * @return
	 */
	public static Builder builder(@Nonnull Frame frame) {
		return new Builder(frame);
	}


	// --- Inner Classes ---

	/**
	 * A {@link Frame} builder.
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [15 Jul 2016]
	 */
	public static class Builder {
		private final Command command;
		private final Map<Header, List<String>> headers = new LinkedHashMap<>();
		private ByteBuffer body;

		/**
		 * Create a {@link Frame} builder from the given {@link Builder}.
		 * 
		 * @param builder
		 */
		private Builder(@Nonnull Builder builder) {
			this(builder.command);

			for (Entry<Header, List<String>> e : builder.headers.entrySet()) {
				headers.put(e.getKey(),  new ArrayList<>(e.getValue()));
			}
			this.body = builder.body;
		}	

		/**
		 * Create a {@link Frame} builder from the given {@link Frame}.
		 * 
		 * @param frame
		 */
		private Builder(@Nonnull Frame frame) {
			this(frame.getCommand());

			for (Entry<Header, List<String>> e : frame.getHeaders().entrySet()) {
				headers.put(e.getKey(),  new ArrayList<>(e.getValue()));
			}
			this.body = frame.getBody();
		}

		/**
		 * Create a {@link Frame} for the {@link Command}. 
		 * 
		 * @param command
		 */
		private Builder(@Nonnull Command command) {
			this.command = command;
		}

		/**
		 * 
		 * @param header
		 * @param values
		 * @return
		 */
		public Builder header(@Nonnull Header header, @Nonnull String... values) {
			if (values == null || values.length == 0)
				throw new IllegalArgumentException("'values' cannot be null or empty!");

			final StringJoiner joiner = new StringJoiner(",");
			for (String v : values) {
				joiner.add(v);
			}

			List<String> valueList = this.headers.get(header);
			if (valueList == null) {
				this.headers.put(header, valueList = new ArrayList<>());
			}
			valueList.add(joiner.toString());
			return this;
		}

		/**
		 * 
		 * @param headers
		 * @return
		 */
		public Builder headers(Map<Header, String> headers) {
			for (Entry<Header, String> e : headers.entrySet()) {
				header(e.getKey(), e.getValue());
			}
			return this;
		}

		/**
		 * 
		 * @param destination
		 * @return
		 */
		public Builder destination(@Nonnull String destination) {
			if (!this.command.destination()) {
				throw new IllegalArgumentException(this.command + " does not accept a destination!");
			}
			header(DESTINATION, destination);
			return this;
		}

		/**
		 * 
		 * @param messageId
		 * @return
		 */
		public Builder messageId(@Nonnull String messageId) {
			header(MESSAGE_ID, messageId);
			return this;
		}

		/**
		 * Custom Header: send the message to 
		 * 
		 * @param sessionId
		 * @return
		 */
		public Builder session(@Nonnull String session) {
			header(SESSION, session);
			return this;
		}

		/**
		 * 
		 * @param body
		 * @return
		 * @throws IllegalArgumentException if the command type does not accept a body or {@code body} is {@code null}.
		 */
		public Builder body(MediaType contentType, @Nonnull String body) {
			return body(contentType, UTF_8.encode(requireNonNull(body)));
		}

		/**
		 * 
		 * @param contentType
		 * @param body
		 * @return
		 * @throws IllegalArgumentException if the command type does not accept a body or {@code body} is {@code null}.
		 */
		public Builder body(MediaType contentType, @Nonnull ByteBuffer body) {
			if (!this.command.body()) {
				throw new IllegalArgumentException(this.command + " does not accept a body!");
			}
			this.body = requireNonNull(body);
			header(CONTENT_LENGTH, Integer.toString(body.limit()));
			return contentType == null ? this : header(CONTENT_TYPE, contentType.toString());
		}

		/**
		 * 
		 * @param id
		 * @return
		 */
		public Builder subscription(@Nonnull String id) {
			if (!this.command.subscriptionId()) {
				throw new IllegalArgumentException(this.command + " does not accept a subscription!");
			}

			if (this.command == Command.MESSAGE) { // why is MESSAGE so special?!
				header(SUBSCRIPTION, requireNonNull(id));
			} else {
				header(ID, requireNonNull(id));
			}

			return this;
		}

		/**
		 * 
		 * @param outgoing
		 * @param incoming
		 * @return
		 */
		public Builder heartbeat(@Nonnull int outgoing, @Nonnull int incoming) {
			return header(Standard.HEART_BEAT, Integer.toString(outgoing), Integer.toString(incoming));
		}

		/**
		 * 
		 * @param versions
		 * @return
		 */
		public Builder version(String... versions) {
			return header(VERSION, versions);
		}

		/**
		 * 
		 * @param receiptId
		 * @return
		 */
		public Builder receipt(int receiptId) {
			return header(RECEIPT, Integer.toString(receiptId));
		}

		/**
		 * 
		 * @param receiptId
		 * @return
		 */
		public Builder receiptId(int receiptId) {
			return header(RECEIPT_ID, Integer.toString(receiptId));
		}

		/**
		 * Derives values from other headers if needed.
		 */
		private void derive() {
			if (!this.headers.containsKey(MESSAGE_ID) && this.command == Command.MESSAGE) {
				String messageId = Long.toString(MESSAGE_ID_COUNTER.getAndIncrement());
				if (this.headers.containsKey(SESSION))
					messageId = this.headers.get(SESSION).get(0).concat("-").concat(messageId);
				messageId(messageId);
			}
		}

		/**
		 * Verifies the minimum headers are present.
		 */
		private void verify() {
			switch (this.command) {
			case ACK:
			case NACK:
				assertExists(ID);
				break;
			case BEGIN:
			case COMMIT:
			case ABORT:
				assertExists(TRANSACTION);
				break;	
			case CONNECT:
			case STOMP:
				assertExists(ACCEPT_VERSION);
				assertExists(HOST);
				break;
			case CONNECTED:
				assertExists(VERSION);
				break;
			case DISCONNECT:
			case ERROR:
			case HEARTBEAT:
				break;
			case MESSAGE:
				assertExists(DESTINATION);
				assertExists(MESSAGE_ID);
				assertExists(SUBSCRIPTION);
				break;
			case RECEIPT:
				assertExists(RECEIPT_ID);
				break;
			case SEND:
				assertExists(DESTINATION);
				break;
			case SUBSCRIBE:
				assertExists(DESTINATION);
				assertExists(ID);
				break;
			case UNSUBSCRIBE:
				assertExists(ID);
				break;
			}
		}

		/**
		 * 
		 * @param header
		 */
		private void assertExists(Header header) {
			if (!this.headers.containsKey(header))
				throw new AssertionError(String.format(
						"Required header '%s' not set on '%s' frame!",
						StringUtils.capitalize(header.toString().toLowerCase()), command));
		}

		/**
		 * @return a newly created {@link Frame}.
		 */
		public Frame build() {
			derive();
			verify();

			final Map<Header, List<String>> headers = new LinkedHashMap<>();
			for (Entry<Header, List<String>> e : this.headers.entrySet()) {
				headers.put(e.getKey(),  new ArrayList<>(e.getValue()));
			}
			return new Frame(this.command, headers, this.body);
		}
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [25 Jul 2016]
	 */
	public static class HeartBeat {
		public final long x, y;

		private HeartBeat(String heartBeat) { 
			if (heartBeat == null) {
				this.x = 0;
				this.y = 0;
				return;
			}
			final String[] tokens = heartBeat.split(",");
			if (tokens.length != 2)
				throw new IllegalStateException("Invalid number of heart beat elements!");
			this.x = Long.parseLong(tokens[0]);
			this.y = Long.parseLong(tokens[1]);
		}
	}
}
