package civvi.stomp;

import static civvi.stomp.Command.CONNECT;
import static civvi.stomp.Command.CONNECTED;
import static civvi.stomp.Command.DISCONNECT;
import static civvi.stomp.Command.SEND;
import static civvi.stomp.Headers.CONTENT_TYPE;
import static civvi.stomp.Headers.HEART_BEAT;
import static civvi.stomp.Headers.HOST;
import static civvi.stomp.Headers.SERVER;
import static civvi.stomp.Headers.SESSION;
import static civvi.stomp.Headers.VERSION;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

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
	private final MultivaluedMap<String, String> headers;
	private final ByteBuffer body;

	public Frame() {
		this(null, null, null);
	}


	/**
	 * 
	 * @param command
	 * @param headers
	 */
	Frame(Command command, MultivaluedMap<String, String> headers) {
		this(command, headers, null);
	}

	/**
	 * 
	 * @param command
	 * @param headers
	 * @param body
	 */
	Frame(Command command, MultivaluedMap<String, String> headers, ByteBuffer body) {
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
	public MultivaluedMap<String, String> getHeaders() {
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
	 * @param key
	 * @return
	 */
	public boolean containsHeader(String key) {
		return getHeaders(key) != null;
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
		return getHeaders().getFirst(key);
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
			final ByteBuffer buffer = getBody();
			buffer.flip(); // flip the buffer for reading
			byte[] bytes = new byte[buffer.remaining()]; // create a byte array the length of the number of bytes written to the buffer
			buffer.get(bytes); // read the bytes that were written
			writer.append(NEW_LINE).append(new String(bytes, StandardCharsets.UTF_8));
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

		final MultivaluedMap<String, String> headers = new MultivaluedHashMap<>(new LinkedCaseInsensitiveMap<>());

		String headerLine;
		while (!(headerLine = reader.readLine()).isEmpty() && !Character.toString(NULL).equals(headerLine)) {
			final String[] tokens = headerLine.split(":");
			List<String> values = headers.get(tokens[0]);
			if (values == null) {
				headers.put(tokens[0], values = new ArrayList<>());
			}
			values.add(tokens[1]);
		}

		final StringBuilder buf = new StringBuilder();
		final char[] arr = new char[8 * 1024];
		int numCharsRead;
		while ((numCharsRead = reader.read(arr, 0, arr.length)) != -1) {
			buf.append(arr, 0, numCharsRead);
		}
		final ByteBuffer byteBuf = buf.length() == 0 ? null : ByteBuffer.wrap(buf.toString().getBytes(StandardCharsets.UTF_8));
		return new Frame(command, headers, byteBuf);
	}

	/**
	 * 
	 * @param host
	 * @return
	 */
	public static Builder connect(String host) {
		return builder(CONNECT).header(HOST, host);
	}

	/**
	 * 
	 * @param receiptId
	 * @return
	 */
	public static Builder disconnect() {
		return builder(DISCONNECT);
	}

	/**
	 * 
	 * @param receiptId
	 * @return
	 */
	public static Builder error() {
		return builder(Command.ERROR);
	}

	/**
	 * 
	 * @param destination
	 * @param contentType
	 * @param body
	 * @return
	 */
	public static Builder send(String destination, MediaType contentType, String body) {
		return builder(SEND).body(contentType, body);
	}

	/**
	 * 
	 * @param version
	 * @param session
	 * @param server
	 * @param heartBeat
	 * @return
	 */
	public static Builder connnected(String version, String session, String server) {
		final Builder builder = builder(CONNECTED).header(VERSION, version);
		if (session != null)
			builder.header(SESSION, session);
		if (server != null)
			builder.header(SERVER, server);
		return builder;
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
		private ByteBuffer body;

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
			if (values == null || values.length == 0)
				throw new IllegalArgumentException("'values' cannot be null or empty!");

			final StringJoiner joiner = new StringJoiner(",");
			for (String v : values) {
				joiner.add(v);
			}

			this.headers.compute(key, (k, v) -> { v = v != null ? v : new ArrayList<>(); v.add(joiner.toString()); return v; });
			return this;
		}

		/**
		 * 
		 * @param body
		 * @return
		 * @throws IllegalArgumentException if the command type does not accept a body or {@code body} is {@code null}.
		 */
		public Builder body(MediaType contentType, String body) {
			return body(contentType, ByteBuffer.wrap(body.getBytes(StandardCharsets.UTF_8)));
		}

		/**
		 * 
		 * @param body
		 * @return
		 * @throws IllegalArgumentException if the command type does not accept a body or {@code body} is {@code null}.
		 */
		public Builder body(MediaType contentType, ByteBuffer body) {
			if (!this.command.body()) {
				throw new IllegalArgumentException(this.command + " does not accept a body!");
			}
			if (body == null) {
				throw new IllegalArgumentException("'body' cannot be null!");
			}
			this.body = body;
			return header(CONTENT_TYPE, contentType.toString());
		}

		/**
		 * 
		 * @param outgoing
		 * @param incoming
		 * @return
		 */
		public Builder heartbeat(int outgoing, int incoming ) {
			return header(HEART_BEAT, Integer.toString(outgoing), Integer.toString(incoming));
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
		public Builder reciept(int receiptId) {
			return header(Headers.RECEIPT, Integer.toString(receiptId));
		}

		/**
		 * @return a newly created {@link Frame}.
		 */
		public Frame build() {
			final MultivaluedMap<String, String> headers = new MultivaluedHashMap<>(new LinkedCaseInsensitiveMap<>());
			for (Entry<String, List<String>> e : this.headers.entrySet()) {
				headers.put(e.getKey(),  new ArrayList<>(e.getValue()));
			}
			return new Frame(this.command, headers, this.body);
		}
	}
}
