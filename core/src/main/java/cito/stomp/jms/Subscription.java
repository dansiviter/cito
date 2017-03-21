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
import javax.jms.MessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cito.stomp.Frame;
import cito.stomp.Headers;

/**
 * Defines a subscription
 * 
 * @author Daniel Siviter
 * @since v1.0 [21 Jul 2016]
 */
public class Subscription implements MessageListener {
	private static final Logger LOG = LoggerFactory.getLogger(Subscription.class);
	private static final String SELECTOR = "session IS NULL OR session = '%s'";
	private static final String COMPLEX_SELECTOR = "(session IS NULL OR session = '%s') AND %s";

	private final Session session;
	private final String id;
	private final Destination destination;
	private final MessageConsumer consumer;

	/**
	 * 
	 * @param session
	 * @param id
	 * @param frame
	 * @throws JMSException
	 */
	public Subscription(Session session, String id, Frame frame, Factory factory) throws JMSException {
		this.session = session;
		this.id = id;
		this.destination = session.toDestination(frame.getFirstHeader(Headers.DESTINATION));

		final String sessionId = this.session.getConnection().getSessionId();
		// only consume messages that are for everyone OR only for me
		String selector = frame.getFirstHeader(Headers.SELECTOR);
		if (selector == null) {
			selector = String.format(SELECTOR, sessionId);
		} else {
			selector = String.format(COMPLEX_SELECTOR, sessionId, selector);
		}

		this.consumer = session.createConsumer(this.destination, selector);
		this.consumer.setMessageListener(this);

		LOG.debug("Created subscription. [sessionId={},id={},destination={},selector={}]", sessionId, id, this.destination, selector);
	}

	/**
	 * 
	 * @return
	 */
	public String getSubscriptionId() {
		return this.id;
	}

	/**
	 * 
	 * @return
	 */
	public Destination getDestination() {
		return this.destination;
	}

	@Override
	public void onMessage(Message message) {
		try {
			int ackMode = this.session.getAcknowledgeMode();
			if (ackMode == javax.jms.Session.CLIENT_ACKNOWLEDGE) {
				synchronized (this) {
					((Connection) this.session.getConnection()).addAckMessage(message);
				}
			}
			this.session.send(message, this);
		} catch (JMSException | IOException e) {
			LOG.error("Unable to send message! [sessionId={},subscriptionId={}]",
					this.session.getConnection().getSessionId(), this.id);
		}
	}

	/**
	 * 
	 * @throws JMSException
	 */
	public void close() throws JMSException {
		this.consumer.close();
	}
}
