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

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cito.stomp.Frame;
import cito.stomp.Header.Custom;

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
	public Subscription(@Nonnull Session session, @Nonnull String id, @Nonnull Frame frame) throws JMSException {
		this.session = requireNonNull(session);
		this.id = requireNonNull(id);
		this.destination = session.toDestination(frame.destination().get());

		final String sessionId = this.session.getConnection().getSessionId();
		// only consume messages that are for everyone OR only for me
		final Optional<String> selector = frame.getFirst(Custom.SELECTOR);
		final String selectorStr;
		if (selector.isPresent()) {
			selectorStr = String.format(COMPLEX_SELECTOR, sessionId, selector.get());
		} else {
			selectorStr = String.format(SELECTOR, sessionId);
		}

		this.consumer = session.createConsumer(this.destination, selectorStr);
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
