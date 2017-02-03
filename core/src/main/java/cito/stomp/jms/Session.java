package cito.stomp.jms;

import java.io.IOException;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;

import cito.stomp.Frame;

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
	 * @return
	 * @throws JMSException
	 */
	public MessageProducer getProducer() throws JMSException {
		if (this.producer == null) {
			this.producer = withSession(s -> s.createProducer(null));
		}
		return this.producer;
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
		String destinationName = frame.destination();
		final Message message = withSession(s -> this.factory.toMessage(s, frame));
		final Destination destination = withSession(s -> this.factory.toDestination(s, destinationName));
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
}
