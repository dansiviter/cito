package cito.stomp.jms;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import cito.LinkedCaseInsensitiveMap;
import cito.stomp.Command;
import cito.stomp.Frame;
import cito.stomp.Headers;
import cito.stomp.Frame.Builder;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [21 Jul 2016]
 */
public class Session {
	private static final Set<String> IGNORE_HEADERS;

	static {
		final Set<String> ignore = new HashSet<>();
		ignore.add(Headers.DESTINATION);
		ignore.add(Headers.CONTENT_LENGTH);
		IGNORE_HEADERS = Collections.unmodifiableSet(ignore);
	}

	private final AbstractConnection conn;
	private final javax.jms.Session delegate;

	private MessageProducer producer;

	Session(AbstractConnection conn, javax.jms.Session delegate) {
		this.conn = conn;
		this.delegate = delegate;
	}

	public AbstractConnection getConnection() {
		return this.conn;
	}

	public javax.jms.Session getDelegate() {
		return this.delegate;
	}

	public MessageProducer getProducer() throws JMSException {
		if (this.producer == null) {
			this.producer = this.delegate.createProducer(null);
		}
		return this.producer;
	}

	/**
	 * 
	 * @param frame
	 * @throws JMSException
	 */
	public void send(Frame frame) throws JMSException {
		String destinationName = frame.getFirstHeader(Headers.DESTINATION);
		final Message message = toMessage(frame);
		final Destination destination = toDestination(destinationName);
		getProducer().send(destination, message);
	}

	/**
	 * 
	 * @param message
	 * @param subscription
	 * @throws JMSException
	 * @throws IOException 
	 */
	public void send(Message message, Subscription subscription) throws JMSException, IOException {
		if (getDelegate().getAcknowledgeMode() == javax.jms.Session.CLIENT_ACKNOWLEDGE) {
			((Connection) this.conn).addAckMessage(message);
		}
		final Frame frame = toFrame(message, subscription.getSubscriptionId());
		this.conn.send(frame);
	}

	/**
	 * 
	 * @param frame
	 * @return
	 * @throws JMSException
	 */
	private Message toMessage(Frame frame) throws JMSException {
		final Message msg;
		byte[] bytes = frame.getBody().array();
		if (frame.containsHeader(Headers.CONTENT_LENGTH)) {
			BytesMessage bm = this.delegate.createBytesMessage();
			bm.writeBytes(bytes);
			msg = bm;
		} else {
			final String body = new String(bytes, StandardCharsets.UTF_8);
			msg = this.delegate.createTextMessage(body);
		}
		copyHeaders(frame, msg);
		return msg;
	}

	/**
	 * 
	 * @param message
	 * @param subscriptionId
	 * @return
	 * @throws IOException
	 * @throws JMSException
	 */
	private Frame toFrame(Message message, String subscriptionId) throws IOException, JMSException {
		Builder frame = Frame.builder(Command.MESSAGE).header(Headers.SUBSCRIPTION, subscriptionId);
		copyHeaders(message, frame);

		final String contentType = message.getStringProperty(Headers.CONTENT_TYPE);
		final ByteBuffer buf;
		if (message instanceof TextMessage) {
			final TextMessage msg = (TextMessage) message;
			buf = ByteBuffer.wrap(msg.getText().getBytes(StandardCharsets.UTF_8));
		} else if (message instanceof BytesMessage) {
			final BytesMessage msg = (BytesMessage) message;
			byte[] data = new byte[(int) msg.getBodyLength()];
			msg.readBytes(data);
			frame.header(Headers.CONTENT_LENGTH, Integer.toString(data.length));
			buf = ByteBuffer.wrap(data);
		} else {
			throw new IllegalArgumentException("Unexpected type! [" + message.getClass() + "]");
		}
		frame.body(contentType == null ? null : MediaType.valueOf(contentType), buf);
		return frame.build();
	}

	/**
	 * 
	 * @param session
	 * @param name
	 * @return
	 * @throws JMSException
	 */
	Destination toDestination(String name) throws JMSException {
		final int separatorIndex = name.indexOf('/', 1);
		final String type = name.substring(0, separatorIndex + 1).toLowerCase();   
		name = name.substring(separatorIndex + 1, name.length());
		switch (type) {
		case "/queue/":
			return this.delegate.createQueue(name);
		case "/topic/":
			return this.delegate.createTopic(name);
		default:
			throw new IllegalArgumentException("Unknown destination! [type=" + type + ",name="  + name + "]");

		}
	}

	/**
	 * 
	 * @param d
	 * @return
	 * @throws JMSException
	 */
	private String toDestination(Destination d) throws JMSException {
		if (d == null)  return null;

		final StringBuilder buffer = new StringBuilder("/");
		if (d instanceof TemporaryTopic || d instanceof TemporaryQueue)
			buffer.append("temp-");

		if (d instanceof Topic) {
			final Topic topic = (Topic) d;
			buffer.append("topic/").append(topic.getTopicName());
		} else {
			final Queue queue = (Queue) d;
			buffer.append("queue/").append(queue.getQueueName());
		}
		return buffer.toString();
	}

	/**
	 * 
	 * @param message
	 * @param frame
	 * @throws IOException
	 * @throws JMSException
	 */
	private void copyHeaders(Message message, Builder frame) throws IOException, JMSException {
		frame.header(Headers.DESTINATION, toDestination(message.getJMSDestination()));
		frame.header(Headers.MESSAGE_ID, message.getJMSMessageID());

		if (message.getJMSCorrelationID() != null) {
			frame.header(Headers.CORRELATION_ID, message.getJMSCorrelationID());
		}
		frame.header(Headers.EXPIRATION_TIME, "" + message.getJMSExpiration());

		if (message.getJMSRedelivered()) {
			frame.header(Headers.REDELIVERED, "true");
		}
		frame.header(Headers.PRORITY, Integer.toString(message.getJMSPriority()));

		if (message.getJMSReplyTo() != null) {
			frame.header(Headers.REPLY_TO, toDestination(message.getJMSReplyTo()));
		}
		frame.header(Headers.TIMESTAMP, Long.toString(message.getJMSTimestamp()));

		if (message.getJMSType() != null) {
			frame.header(Headers.TYPE, message.getJMSType());
		}

		@SuppressWarnings("unchecked")
		final Enumeration<String> names = message.getPropertyNames();
		while (names.hasMoreElements()) {
			final String name = names.nextElement();
			frame.header(toStompKey(name), message.getStringProperty(name));
		}
	}

	/**
	 * 
	 * @param frame
	 * @param msg
	 * @throws JMSException
	 */
	protected void copyHeaders(Frame frame, Message msg) throws JMSException {
		final MultivaluedMap<String, String> headers = new MultivaluedHashMap<>(new LinkedCaseInsensitiveMap<>());
		headers.putAll(frame.getHeaders());

		msg.setJMSCorrelationID(removeAndGetFirst(headers, Headers.CORRELATION_ID));

		final String type = removeAndGetFirst(headers, Headers.TYPE);
		if (type != null) {
			msg.setJMSType(type);
		}

		String replyTo = removeAndGetFirst(headers, Headers.REPLY_TO);
		if (replyTo != null) {
			msg.setJMSReplyTo(toDestination(replyTo));
		}

		final String sessionId = removeAndGetFirst(headers, Headers.SESSION);
		if (sessionId != null)
			msg.setStringProperty("session", sessionId);

		// now the general headers
		for (Entry<String, List<String>> e : frame.getHeaders().entrySet()) {
			if (IGNORE_HEADERS.contains(e.getKey()))
				continue;
			msg.setStringProperty(toJmsKey(e.getKey()), e.getValue().get(0));
		}
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public static String toJmsKey(String key) {
		return key.replace("-", "_HYPHEN_").replace(".", "_DOT_");
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public static String toStompKey(String key) {
		return key.replace("_HYPHEN_", "-").replace("_DOT_", ".");
	}

	/**
	 * @throws JMSException 
	 * 
	 */
	public void close() throws JMSException {
		this.delegate.close();
	}

	private static String removeAndGetFirst(MultivaluedMap<String, String> map, String key) {
		final List<String> values = map.remove(key);
		return values != null && !values.isEmpty() ? values.get(0) : null;
	}
}
