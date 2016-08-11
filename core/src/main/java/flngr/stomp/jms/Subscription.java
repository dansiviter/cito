package flngr.stomp.jms;

import java.io.IOException;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import flngr.stomp.Frame;
import flngr.stomp.Headers;

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
	public Subscription(Session session, String id, Frame frame) throws JMSException {
		this.session = session;
		this.id = id;
		this.destination = session.toDestination(frame.getFirstHeader(Headers.DESTINATION));

		// only consume messages that are for everyone OR only for me
		String selector = frame.getFirstHeader(Headers.SELECTOR);
		if (selector == null) {
			selector = String.format(SELECTOR, this.session.getConnection().getSessionId());
		} else {
			selector = String.format(COMPLEX_SELECTOR, this.session.getConnection().getSessionId(), selector);
		}

		this.consumer = session.getDelegate().createConsumer(this.destination, selector);
		this.consumer.setMessageListener(this);
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
			int ackMode = session.getDelegate().getAcknowledgeMode();
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
