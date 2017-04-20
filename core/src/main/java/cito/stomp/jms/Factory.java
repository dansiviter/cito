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
package cito.stomp.jms;

import static java.nio.charset.StandardCharsets.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import cito.collections.LinkedCaseInsensitiveMap;
import cito.stomp.Command;
import cito.stomp.Frame;
import cito.stomp.Frame.Builder;
import cito.stomp.Headers;

/**
 * Factory for creating both JMS and STOMP artifacts.
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Oct 2016]
 */
@ApplicationScoped
public class Factory {
	private static final Set<String> IGNORE_HEADERS;

	static {
		final Set<String> ignore = new HashSet<>();
		ignore.add(Headers.DESTINATION);
		ignore.add(Headers.CONTENT_LENGTH);
		IGNORE_HEADERS = Collections.unmodifiableSet(ignore);
	}

	/**
	 * 
	 * @param conn
	 * @param transacted
	 * @param acknowledgeMode
	 * @return
	 * @throws JMSException
	 */
	public cito.stomp.jms.Session toSession(AbstractConnection conn, boolean transacted, int acknowledgeMode)
			throws JMSException
	{
		return new cito.stomp.jms.Session(
				conn,
				conn.getDelegate().createSession(transacted, acknowledgeMode),
				this);
	}

	/**
	 * 
	 * @param session
	 * @param name
	 * @return
	 * @throws JMSException
	 */
	public Destination toDestination(Session session, String name) throws JMSException {
		final int separatorIndex = name.indexOf('/', 1);
		final String type = name.substring(0, separatorIndex + 1).toLowerCase();   
		name = name.substring(separatorIndex + 1, name.length());
		switch (type) {
		case "/queue/":
			return session.createQueue(name);
		case "/topic/":
			return session.createTopic(name);
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
	public String fromDestination(Destination d) throws JMSException {
		if (d == null)  return null;

		if (d instanceof TemporaryTopic || d instanceof TemporaryQueue) {
			throw new IllegalArgumentException("Temporary destinations are not supported! [" + d + "]");
		}

		if (d instanceof Topic) {
			final Topic topic = (Topic) d;
			return new StringBuilder("/topic/").append(topic.getTopicName()).toString();
		}
		final Queue queue = (Queue) d;
		return new StringBuilder("/queue/").append(queue.getQueueName()).toString();
	}

	/**
	 * 
	 * @param session
	 * @param frame
	 * @return
	 * @throws JMSException
	 */
	public Message toMessage(Session session, Frame frame) throws JMSException {
		final Message msg;
		if (frame.containsHeader(Headers.CONTENT_LENGTH)) {
			final ByteBuffer buf = frame.getBody();
			byte[] bytes = new byte[buf.remaining()];
			buf.get(bytes);
			final BytesMessage bm = session.createBytesMessage();
			bm.writeBytes(bytes);
			msg = bm;
		} else {
			msg = session.createTextMessage(UTF_8.decode(frame.getBody()).toString());
		}
		copyHeaders(session, frame, msg);
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
	public Frame toFrame(Message message, String subscriptionId) throws IOException, JMSException {
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
	 * @param message
	 * @param frame
	 * @throws IOException
	 * @throws JMSException
	 */
	private void copyHeaders(Message message, Builder frame) throws IOException, JMSException {
		frame.header(Headers.DESTINATION, fromDestination(message.getJMSDestination()));
		frame.header(Headers.MESSAGE_ID, message.getJMSMessageID());

		if (message.getJMSCorrelationID() != null) {
			frame.header(Headers.CORRELATION_ID, message.getJMSCorrelationID());
		}
		frame.header(Headers.EXPIRATION_TIME, Long.toString(message.getJMSExpiration()));

		if (message.getJMSRedelivered()) {
			frame.header(Headers.REDELIVERED, "true");
		}
		frame.header(Headers.PRORITY, Integer.toString(message.getJMSPriority()));

		if (message.getJMSReplyTo() != null) {
			frame.header(Headers.REPLY_TO, fromDestination(message.getJMSReplyTo()));
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
	private void copyHeaders(Session session, Frame frame, Message msg) throws JMSException {
		final MultivaluedMap<String, String> headers = new MultivaluedHashMap<>(new LinkedCaseInsensitiveMap<>());
		headers.putAll(frame.getHeaders());

		msg.setJMSCorrelationID(removeAndGetFirst(headers, Headers.CORRELATION_ID));

		final String type = removeAndGetFirst(headers, Headers.TYPE);
		if (type != null) {
			msg.setJMSType(type);
		}

		String replyTo = removeAndGetFirst(headers, Headers.REPLY_TO);
		if (replyTo != null) {
			msg.setJMSReplyTo(toDestination(session, replyTo));
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


	// --- Static Methods ---

	/**
	 * 
	 * @param map
	 * @param key
	 * @return
	 */
	private static String removeAndGetFirst(MultivaluedMap<String, String> map, String key) {
		final List<String> values = map.remove(key);
		return values != null && !values.isEmpty() ? values.get(0) : null;
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
}
