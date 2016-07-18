package civvi.stomp;

import static civvi.stomp.Command.CONNECT;
import static civvi.stomp.Command.CONNECTED;
import static civvi.stomp.Command.DISCONNECT;
import static civvi.stomp.Command.SEND;
import static civvi.stomp.Headers.ACCEPT_VERSION;
import static civvi.stomp.Headers.CONTENT_TYPE;
import static civvi.stomp.Headers.HEART_BEAT;
import static civvi.stomp.Headers.HOST;
import static civvi.stomp.Headers.RECEIPT_ID;
import static civvi.stomp.Headers.SERVER;
import static civvi.stomp.Headers.SESSION;
import static civvi.stomp.Headers.VERSION;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import javax.ws.rs.core.MediaType;

import civvi.LinkedCaseInsensitiveMap;

/**
 * Defines a STOMP frame
 * 
 * @author Daniel Siviter
 * @since v1.0 [12 Jul 2016]
 */
public class Frame {
	static final char NULL = '\u0000';
	static final char NEW_LINE = '\n';

	private final Command command;
	private final Map<String, List<String>> headers;
	private final String body;

	public Frame() {
		this(null, null, null);
	}


	/**
	 * 
	 * @param command
	 * @param headers
	 */
	Frame(Command command, Map<String, List<String>> headers) {
		this(command, headers, null);
	}

	/**
	 * 
	 * @param command
	 * @param headers
	 * @param body
	 */
	Frame(Command command, Map<String, List<String>> headers, String body) {
		this.command = command;
		this.headers = headers;
		this.body = body;
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
	public Map<String, List<String>> getHeaders() {
		return headers;
	}

	/**
	 * 
	 * @return
	 */
	public String getBody() {
		return body;
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public List<String> getHeaders(String key) {
		return getHeaders().get(key);
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public String getFirstHeader(String key) {
		final List<String> values = getHeaders(key);
		return values != null && !values.isEmpty() ? values.get(0) : null;
	}

	/**
	 * 
	 * @return
	 */
	public int getContentLength() {
		final String contentLength = getFirstHeader(Headers.CONTENT_LENGTH);
		return contentLength != null ? Integer.parseInt(contentLength) : -1;
	}

	/**
	 * 
	 * @return
	 */
	public MediaType getContentType() {
		final String contentType = getFirstHeader(Headers.CONTENT_TYPE);
		return contentType != null ? MediaType.valueOf(contentType) : null;
	}

	/**
	 * 
	 * @return
	 */
	public String getReceipt() {
		return getFirstHeader(Headers.RECEIPT);
	}

	/**
	 * 
	 * @param writer
	 * @throws IOException 
	 */
	public void to(Writer writer) throws IOException {
		writer.append(getCommand().name()).append(NEW_LINE);
		// FIXME need to ensure this orders in same order as it came in (case insensitive LinkedHashMap?)
		for (Entry<String, List<String>> e : getHeaders().entrySet()) {
			for (String value : e.getValue()) {
				writer.append(e.getKey()).append(':').append(value).append(NEW_LINE);
			}
		}

		if (getBody() != null) {
			writer.append(NEW_LINE).append(getBody());
		}

		writer.append(NULL);
	}

	/**
	 * FIXME only rough implementation, may not conform to spec yet
	 */
	@Override
	public String toString() {
		try (StringWriter writer = new StringWriter()) {
			to(writer);
			return writer.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	// --- Static Methods ---


	/**
	 * Create a {@code Frame} from a {@link String}.
	 * 
	 * @param in
	 * @return
	 */
	public static Frame from(String in) {
		try (StringReader reader = new StringReader(in)) {
			return from(reader);
		} catch (IOException e) {
			throw new IllegalArgumentException("String not parsable!", e);
		}
	}

	/**
	 * Create a {@code Frame} from a {@link Reader}.
	 * </p>
	 * <strong>Note:</strong> the caller takes responsibility for closing the {@link Reader}.
	 * 
	 * @param in
	 * @return
	 * @throws IOException 
	 */
	public static Frame from(Reader in) throws IOException {
		final BufferedReader reader = new BufferedReader(in);
		final Command command = Command.valueOf(reader.readLine());

		final Map<String, List<String>> headers = new LinkedCaseInsensitiveMap<>();

		String headerLine;
		while (!(headerLine = reader.readLine()).isEmpty() && !Character.toString(NULL).equals(headerLine)) {
			final String[] tokens = headerLine.split(":");
			List<String> values = headers.get(tokens[0]);
			if (values == null) {
				headers.put(tokens[0], values = new ArrayList<>());
			}
			values.add(tokens[1]);
		}

		final StringBuilder buffer = new StringBuilder();
		final char[] arr = new char[8 * 1024];
		int numCharsRead;
		while ((numCharsRead = reader.read(arr, 0, arr.length)) != -1) {
			buffer.append(arr, 0, numCharsRead);
		}

		return new Frame(command, Collections.unmodifiableMap(headers), buffer.length() == 0 ? null : buffer.toString());
	}

	/**
	 * 
	 * @param host
	 * @return
	 */
	public static Frame connect(String host) {
		return builder(CONNECT).header(ACCEPT_VERSION, "1.1").header(HOST, host).build();
	}

	/**
	 * 
	 * @param receiptId
	 * @return
	 */
	public static Frame disconnect(Integer receiptId) {
		final Builder builder = builder(DISCONNECT);
		if (receiptId != null)
			builder.header(RECEIPT_ID, receiptId.toString());
		return builder.build();
	}

	/**
	 * 
	 * @param destination
	 * @param contentType
	 * @param body
	 * @return
	 */
	public static Frame send(String destination, MediaType contentType, String body) {
		final Builder builder = builder(SEND).body(body);
		if (contentType != null)
			builder.header(CONTENT_TYPE, contentType.toString());
		return builder.build();
	}

	/**
	 * 
	 * @param version
	 * @param session
	 * @param server
	 * @param heartBeat
	 * @return
	 */
	public static Object connnected(String version, String session, String server, String heartBeat) {
		final Builder builder = builder(CONNECTED).header(VERSION, version);
		if (session != null)
			builder.header(SESSION, session);
		if (server != null)
			builder.header(SERVER, server);
		if (heartBeat != null)
			builder.header(HEART_BEAT, heartBeat);
		return builder.build();
	}

	/**
	 * 
	 * @param command
	 * @return
	 */
	public static Builder builder(Command command) {
		return new Builder(command);
	}

	/**
	 * 
	 * @param frame
	 * @return
	 */
	public static Builder builder(Frame frame) {
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
		private final Map<String, List<String>> headers = new LinkedCaseInsensitiveMap<>();
		private String body;

		/**
		 * Create a {@link Frame} builder from the given {@link Frame}.
		 * 
		 * @param frame
		 */
		private Builder(Frame frame) {
			this(frame.getCommand());

			for (Entry<String, List<String>> e : frame.getHeaders().entrySet()) {
				headers.put(e.getKey(),  new ArrayList<>(e.getValue()));
			}
			this.body = frame.getBody();
		}

		/**
		 * Create a {@link Frame} for the {@link Command}. 
		 * 
		 * @param command
		 */
		private Builder(Command command) {
			this.command = command;
		}

		/**
		 * 
		 * @param key
		 * @param values
		 * @return
		 */
		public Builder header(String key, String... values) {
			final StringJoiner joiner = new StringJoiner(",");
			for (String value : values)
				joiner.add(value);

			this.headers.compute(key, (k, v) -> { v = v != null ? v : new ArrayList<>(); v.add(joiner.toString()); return v; });
			return this;
		}

		/**
		 * 
		 * @param body
		 * @return
		 * @throws IllegalArgumentException if the command type does not accept a body.
		 */
		public Builder body(String body) {
			if (!this.command.body())
				throw new IllegalArgumentException(this.command + " does not accept a body!");
			this.body = body;
			return this;
		}

		/**
		 * @return a newly created {@link Frame}.
		 */
		public Frame build() {
			final Map<String, List<String>> headers = new LinkedCaseInsensitiveMap<>();
			for (Entry<String, List<String>> e : this.headers.entrySet()) {
				headers.put(e.getKey(),  Collections.unmodifiableList(new ArrayList<>(e.getValue())));
			}
			return new Frame(this.command, Collections.unmodifiableMap(headers), this.body);
		}
	}
}
