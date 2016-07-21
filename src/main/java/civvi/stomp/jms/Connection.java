package civvi.stomp.jms;

import static civvi.stomp.Headers.ACCEPT_VERSION;

import java.io.IOException;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import civvi.messaging.event.Message;
import civvi.stomp.Frame;
import civvi.stomp.Headers;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [21 Jul 2016]
 */
public class Connection {
	private static final Logger LOG = LoggerFactory.getLogger(Connection.class);
	private static final String SUPPORTED_VERSION = "1.2";

	private final Sender sender;
	private final String sessionId;
	private final ConnectionFactory factory;

	private javax.jms.Connection delegate;
	private Session session;

	public Connection(Sender sender, String sessionId, ConnectionFactory factory) throws JMSException {
		this.sender = sender;
		this.sessionId = sessionId;
		this.factory = factory;
	}

	/**
	 * 
	 * @param frame
	 * @throws IOException
	 * @throws EncodeException
	 */
	public void send(Frame frame) {
		LOG.info("Senging message to client. [sessionId={},command={}]", sessionId, frame.getCommand());
		this.sender.send(new Message(this.sessionId, frame));
	}

	/**
	 * 
	 * @return
	 * @throws JMSException
	 */
	public Session getSession() throws JMSException {
		if (this.session == null) {
			this.session = new Session(delegate.createSession());
		}
		return this.session;
	}

	/**
	 * 
	 * @param connectFrame 
	 * @throws DeploymentException
	 * @throws IOException
	 */
	public Connection open(Message msg) throws JMSException {
		LOG.info("Opening connection. [sessionId={}]", this.sessionId);
		if (this.delegate != null) {
			throw new IllegalStateException("Already connected!");
		}

		if (!msg.frame.containsHeader(ACCEPT_VERSION) || !msg.frame.getHeaders(ACCEPT_VERSION).contains(SUPPORTED_VERSION)) {
			final Frame.Builder error = Frame.error().version(SUPPORTED_VERSION);
			error.body(MediaType.TEXT_PLAIN_TYPE, "Only STOMP v1.2 supported!");
			send(error.build());
			throw new IllegalStateException("Only STOMP v1.2 supported!" + msg.frame.getHeaders(Headers.ACCEPT_VERSION));
		}

		final String login = msg.frame.getFirstHeader(Headers.PASSCODE);
		final String passcode = msg.frame.getFirstHeader(Headers.PASSCODE);

		if (login != null) {
			this.delegate = this.factory.createConnection(login, passcode);
		} else {
			this.delegate = this.factory.createConnection();
		}

		this.delegate.setClientID(msg.sessionId);
		this.delegate.start();

		final Frame connected = Frame.connnected(SUPPORTED_VERSION, this.sessionId, "localhost").build();
		send(connected);
		return this;
	}

	public void close() throws JMSException {
		LOG.info("Closing connection. [sessionId={}]", this.sessionId);
		this.delegate.close();
	}
}
