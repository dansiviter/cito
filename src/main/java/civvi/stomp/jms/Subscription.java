package civvi.stomp.jms;

import java.io.IOException;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import civvi.stomp.Frame;
import civvi.stomp.Headers;

/**
 * Defines a subscription
 * 
 * @author Daniel Siviter
 * @since v1.0 [21 Jul 2016]
 */
public class Subscription implements MessageListener {
	private static final Logger LOG = LoggerFactory.getLogger(Subscription.class);

	private final Session session;
	private final String subscriptionId;
	private final Destination destination;
	private final MessageConsumer consumer;

	/**
	 * 
	 * @param session
	 * @param subscriptionId
	 * @param frame
	 * @throws JMSException
	 */
	public Subscription(Session session, String subscriptionId, Frame frame) throws JMSException {
		this.subscriptionId = subscriptionId;
		this.session = session;
		this.destination = session.toDestination(frame.getFirstHeader(Headers.DESTINATION));
		this.consumer = session.getDelegate().createConsumer(this.destination);
		this.consumer.setMessageListener(this);
	}

	/**
	 * 
	 * @return
	 */
	public String getSubscriptionId() {
		return this.subscriptionId;
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
			this.session.send(message, this);
		} catch (JMSException | IOException e) {
			LOG.error("Unable to send message! [sessionId={},subscriptionId={}]",
					this.session.getConnection().getSessionId(), this.subscriptionId);
		}
	}

	/**
	 * 
	 * @throws JMSException
	 */
	public void close() throws JMSException {
		consumer.close();
	}
}
