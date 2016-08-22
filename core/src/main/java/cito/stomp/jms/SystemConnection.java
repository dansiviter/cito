package cito.stomp.jms;

import java.io.IOException;

import javax.jms.JMSException;

import cito.stomp.Frame;
import cito.stomp.server.event.Message;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [2 Aug 2016]
 */
public class SystemConnection extends AbstractConnection {
	static final String SESSION_ID = "$Y$TEM";
	private Session session;

	@Override
	public String getSessionId() {
		return SESSION_ID;
	}

	@Override
	public void send(Frame frame) throws IOException {
		throw new UnsupportedOperationException("Cannot sent to client from system connection!");
	}

	/**
	 * 
	 * @return
	 * @throws JMSException
	 */
	private Session getSession() throws JMSException {
		if (this.session == null) {
			this.session = new Session(this, getDelegate().createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE));
		}
		return this.session;
	}

	@Override
	public void on(Message msg) {
		final String sessionId = msg.getFrame().session();
		if (!getSessionId().equals(sessionId) && sessionId != null) {
			throw new IllegalArgumentException("Session identifier mismatch! [expected=" + getSessionId() + " OR null,actual=" + msg.sessionId + "]");
		}

		try {
			getSession().send(msg.frame);
		} catch (JMSException e) {
			this.log.error("Error handling message! [sessionId={},command={}]", getSessionId(), msg.frame.getCommand());
		}
	}

	/**
	 * 
	 */
	public void connect() {
		try {
			createDelegate(null, null);
		} catch (JMSException e) {
			throw new IllegalStateException("Unable to create system connection!", e);
		}
	}
}
