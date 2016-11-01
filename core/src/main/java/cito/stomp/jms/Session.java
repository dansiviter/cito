package cito.stomp.jms;

import java.io.IOException;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;

import cito.stomp.Frame;
import cito.stomp.Headers;

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
		String destinationName = frame.getDestination();
		final Message message = this.factory.toMessage(this.delegate, frame);
		final Destination destination = this.factory.toDestination(this.delegate, destinationName);
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
		final Frame frame = this.factory.toFrame(message, subscription.getSubscriptionId());
		this.conn.send(frame);
	}

	/**
	 * @throws JMSException 
	 * 
	 */
	public void close() throws JMSException {
		this.delegate.close();
	}
}
