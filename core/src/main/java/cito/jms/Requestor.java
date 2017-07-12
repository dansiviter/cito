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
package cito.jms;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.jms.Destination;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueRequestor;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.Topic;
import javax.jms.TopicRequestor;

/**
 * A JMS 2.0 version of {@link TopicRequestor} or {@link QueueRequestor}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [20 Apr 2017]
 * @see TopicRequestor
 * @see QueueRequestor
 */
public class Requestor implements AutoCloseable {
	private final JMSContext context;
	private final Destination dest;
	private final JMSConsumer consumer;
	private final JMSProducer producer;
	private final Destination tempDest;

	/**
	 * Constructor for the {@code Requestor} class.
	 * 
	 * <P>This implementation assumes the session parameter to be non-transacted,
	 * with a delivery mode of either {@code AUTO_ACKNOWLEDGE} or 
	 * {@code DUPS_OK_ACKNOWLEDGE}.
	 *
	 * @param context the {@code JMSContext} the destination belongs to.
	 * @param dest the destination to perform the request/reply call on.
	 * @throws JMSException if the JMS provider fails to create the {@code Requestor} due to some internal error.
	 * @throws InvalidDestinationException if an invalid destination is specified.
	 */ 
	public Requestor(JMSContext context, Destination dest) throws JMSException {
		this.context = requireNonNull(context);
		this.dest = requireNonNull(dest);

		this.producer = context.createProducer();
		this.consumer = context.createConsumer(dest);
		if (dest instanceof Topic) {
			this.tempDest = this.context.createTemporaryTopic();
		} else if (dest instanceof Queue) {
			this.tempDest = this.context.createTemporaryQueue();
		} else {
			throw new IllegalArgumentException("Topic or Queue expected! [" + dest + "]");
		}
	}

	/**
	 * @return the context
	 */
	public JMSContext context() {
		return context;
	}

	/**
	 * Sends a request and waits for a reply. The temporary topic is used for the {@code JMSReplyTo} destination; the
	 * first reply is returned, and any following replies are discarded.
	 *
	 * @param message the message to send.
	 * @return the reply message.
	 * @throws JMSException if the JMS provider fails to complete the request due to some internal error.
	 */
	public Message request(Message message) throws JMSException {
		message.setJMSReplyTo(this.tempDest);
		this.producer.send(this.dest, message);
		return this.consumer.receive();
	}

	/**
	 * 
	 * @param message
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws JMSException
	 */
	public Message request(Message message, int timeout, TimeUnit unit) throws JMSException {
		message.setJMSReplyTo(this.tempDest);
		this.producer.send(this.dest, message);
		return this.consumer.receive(unit.toMillis(timeout));
	}

	/**
	 * 
	 * @param message
	 * @param consumer
	 * @throws JMSException
	 */
	public void request(Message message, Consumer<Message> consumer) throws JMSException {
		message.setJMSReplyTo(this.tempDest);
		this.consumer.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(Message message) {
				consumer.accept(message);
			}
		});
		this.producer.send(this.dest, message);
	}

	/**
	 * Closes the {@code Requestor}.
	 *  
	 * @throws JMSException if the JMS provider fails to close the {@code Requestor} due to some internal error.
	 */
	@Override
	public void close() throws JMSException {
		this.consumer.close();
		if (this.tempDest instanceof TemporaryTopic) {
			((TemporaryTopic) this.tempDest).delete();
		} else {
			((TemporaryQueue) this.tempDest).delete();
		}
	}
}
