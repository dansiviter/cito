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

import java.io.IOException;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;

import cito.stomp.Frame;
import cito.util.ToStringBuilder;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [21 Jul 2016]
 */
public class Session {
	private final Factory factory;
	private final AbstractConnection conn;
	private final javax.jms.Session delegate;

	private MessageProducer producer;

	Session(AbstractConnection conn, javax.jms.Session delegate, Factory factory) {
		this.factory = factory;
		this.conn = conn;
		this.delegate = delegate;
	}

	public AbstractConnection getConnection() {
		return this.conn;
	}

	/**
	 * 
	 * @param consumer
	 * @return
	 * @throws JMSException
	 */
	private synchronized <R> R withSession(SessionFunction<R> consumer) throws JMSException {
		return consumer.apply(this.delegate);
	}

	/**
	 * 
	 * @param consumer
	 * @return
	 * @throws JMSException
	 */
	private synchronized void withProducer(ProducerFunction consumer) throws JMSException {
		if (this.producer == null) {
			this.producer = this.delegate.createProducer(null);
		}
		consumer.apply(this.producer);
	}

	/**
	 * 
	 * @return
	 * @throws JMSException
	 */
	public int getAcknowledgeMode() throws JMSException {
		return withSession(javax.jms.Session::getAcknowledgeMode);
	}

	public void commit() throws JMSException {
		this.<Void>withSession(s -> { s.commit(); return null; });	
	}

	public void rollback() throws JMSException {
		this.<Void>withSession(s -> { s.rollback(); return null; });	
	}

	public Destination toDestination(String destination) throws JMSException {
		return withSession(s -> this.factory.toDestination(s, destination));
	}

	/**
	 * 
	 * @param destination
	 * @param selector
	 * @return
	 * @throws JMSException 
	 */
	public MessageConsumer createConsumer(Destination destination, String selector) throws JMSException {
		return withSession(s -> s.createConsumer(destination, selector));
	}

	/**
	 * 
	 * @param frame
	 * @throws JMSException
	 */
	public void sendToBroker(Frame frame) throws JMSException {
		final Message message = withSession(s -> this.factory.toMessage(s, frame));
		final Destination destination = withSession(s -> this.factory.toDestination(s, frame.destination().get()));
		withProducer(p -> p.send(destination, message));
	}

	/**
	 * 
	 * @param message
	 * @param subscription
	 * @throws JMSException
	 * @throws IOException 
	 */
	public void send(Message message, Subscription subscription) throws JMSException, IOException {
		if (getAcknowledgeMode() == javax.jms.Session.CLIENT_ACKNOWLEDGE) {
			((Connection) this.conn).addAckMessage(message);
		}
		final Frame frame = this.factory.toFrame(message, subscription.getSubscriptionId());
		this.conn.sendToClient(frame);
	}

	/**
	 * @throws JMSException 
	 * 
	 */
	public void close() throws JMSException {
		this.<Void>withSession(s -> { s.close(); return null; });
	}

	@Override
	public String toString() {
		return ToStringBuilder
				.create(this)
				.append("connection", getConnection())
				.append("session", this.delegate)
				.toString();
	}


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [3 Feb 2017]
	 * @param <R>
	 */
	@FunctionalInterface
	private interface SessionFunction<R> {
		/**
		 * Applies this function to the given argument.
		 *
		 * @param s the session
		 * @return the function result
		 */
		R apply(javax.jms.Session s) throws JMSException;
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [3 Feb 2017]
	 * @param <R>
	 */
	@FunctionalInterface
	private interface ProducerFunction {
		/**
		 * Applies this function to the given argument.
		 *
		 * @param p the producer
		 * @return the function result
		 */
		void apply(MessageProducer p) throws JMSException;
	}
}
