package cito.stomp.jms;

import java.io.IOException;

import javax.annotation.PreDestroy;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;

import org.slf4j.Logger;

import cito.stomp.server.event.Message;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [2 Aug 2016]
 */
public abstract class AbstractConnection implements cito.stomp.Connection {
	@Inject
	protected Logger log;
	@Inject
	protected BeanManager beanManager;
	@Inject
	protected Relay relay;
	@Inject
	protected ConnectionFactory factory;

	private javax.jms.Connection delegate;

	/**
	 * 
	 * @param msg
	 */
	public abstract void on(Message msg);

	/**
	 * 
	 * @param login
	 * @param passcode
	 * @throws JMSException 
	 */
	protected void createDelegate(String login, String passcode) throws JMSException {
		if (this.delegate != null) {
			throw new IllegalStateException("Already connected!");
		}
		if (login != null) {
			this.delegate = this.factory.createConnection(login, passcode);
		} else {
			this.delegate = this.factory.createConnection();
		}

		this.delegate.setClientID(getSessionId());
		this.delegate.start();
	}

	public javax.jms.Connection getDelegate() {
		return delegate;
	}

	/**
	 * 
	 */
	@PreDestroy
	public void close() {
		try {
			close(new CloseReason(CloseCodes.NORMAL_CLOSURE, null));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void close(CloseReason reason) throws IOException {
		this.log.info("Closing connection. [sessionId={},code={},reason={}]", getSessionId(), reason.getCloseCode().getCode(), reason.getReasonPhrase());
		try {
			this.delegate.close(); // will close JMS Sessions/Producers/Consumers
		} catch (JMSException e) {
			throw new IOException(e);
		}
	}
}
