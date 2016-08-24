package cito.stomp.jms;

import static cito.stomp.Headers.ACCEPT_VERSION;
import static cito.stomp.Headers.HEART_BEAT;
import static org.apache.deltaspike.core.api.provider.BeanProvider.getContextualReference;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.jms.JMSException;
import javax.websocket.CloseReason;
import javax.ws.rs.core.MediaType;

import cito.stomp.Frame;
import cito.stomp.Headers;
import cito.stomp.HeartBeatMonitor;
import cito.stomp.server.event.Message;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [21 Jul 2016]
 */
@Dependent
public class Connection extends AbstractConnection {
	private static final String[] SUPPORTED_VERSIONS = { "1.1", "1.2" };

	private final Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();
	private final Map<String, Session> txSessions = new ConcurrentHashMap<>();
	private final Map<String, javax.jms.Message> ackMessages = new ConcurrentHashMap<>();

	private HeartBeatMonitor heartBeatMonitor;
	private String sessionId;
	private Session session, ackSession;

	/**
	 * Initialise the connection.
	 */
	@PostConstruct
	public void init() {
		final ScheduledExecutorService scheduler = getContextualReference(
				this.beanManager, ScheduledExecutorService.class, false);
		this.heartBeatMonitor = new HeartBeatMonitor(this, scheduler);
	}

	@Override
	public String getSessionId() {
		return sessionId;
	}

	@Override
	public void send(Frame frame) {
		this.heartBeatMonitor.resetSend();
		this.log.info("Senging message to client. [sessionId={},command={}]", this.sessionId, frame.getCommand());
		this.relay.send(new Message(this.sessionId, frame));
	}

	/**
	 * 
	 * @param ack if {@code true} then the client acknowledge session is returned.
	 * @return
	 * @throws JMSException
	 */
	private Session getSession(boolean ack) throws JMSException {
		if (ack) {
			if (this.ackSession == null) {
				this.ackSession = new Session(this, getDelegate().createSession(false, javax.jms.Session.CLIENT_ACKNOWLEDGE));
			}
			return this.ackSession;
		}
		if (this.session == null) {
			this.session = new Session(this, getDelegate().createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE));
		}
		return this.session;
	}

	/**
	 * 
	 * @param in
	 * @return
	 * @throws JMSException
	 */
	private Session getSession(Frame in) throws JMSException {
		final String tx = in.getTransaction();
		if (tx != null)
			return this.txSessions.get(tx);

		final String ackMode = in.getFirstHeader(Headers.ACK);
		return getSession("client".equalsIgnoreCase(ackMode));
	}

	/**
	 * 
	 * @param msg
	 * @return
	 * @throws JMSException
	 */
	public Connection connect(Message msg) throws JMSException {
		this.log.info("Opening connection. [sessionId={}]", this.sessionId);

		this.sessionId = msg.sessionId;
		String version = null;
		final Collection<String> clientSupportedVersion = Arrays.asList(msg.frame.getFirstHeader(ACCEPT_VERSION).split(","));
		for (int i = SUPPORTED_VERSIONS.length - 1; i >= 0; i--) {
			if (clientSupportedVersion.contains(SUPPORTED_VERSIONS[i])) {
				version = SUPPORTED_VERSIONS[i];
				break;
			}
		}

		if (version == null) {
			final Frame.Builder error = Frame.error().version(SUPPORTED_VERSIONS);
			error.body(MediaType.TEXT_PLAIN_TYPE, "Only STOMP v1.2 supported!");
			send(error.build());
			throw new IllegalStateException("Only STOMP v1.2 supported!" + msg.frame.getHeaders(Headers.ACCEPT_VERSION));
		}

		final Frame.Builder connected = Frame.connnected(version, this.sessionId, "localhost");

		final boolean heartBeatEnabled = msg.frame.containsHeader(HEART_BEAT);
		if (heartBeatEnabled) {
			connected.heartbeat(10_000, 10_000);
		}

		final String login = msg.frame.getFirstHeader(Headers.PASSCODE);
		final String passcode = msg.frame.getFirstHeader(Headers.PASSCODE);

		createDelegate(login, passcode);

		send(connected.build());

		if (heartBeatEnabled) {
			final long readDelay = Math.max(msg.frame.getHeartBeat().x, 10_000);
			final long writeDelay = Math.max(10_000, msg.frame.getHeartBeat().y);
			this.heartBeatMonitor.start(readDelay, writeDelay);
		}
		return this;
	}

	/**
	 * 
	 * @param msg
	 */
	@Override
	public void on(Message msg) {
		if (!getSessionId().equals(msg.sessionId)) {
			throw new IllegalArgumentException("Session identifier mismatch! [expected=" + this.sessionId + ",actual=" + msg.sessionId + "]");
		}

		this.heartBeatMonitor.resetRead();

		if (msg.frame.isHeartBeat()) {
			this.log.debug("Heartbeat recieved. [sessionId={}]", this.sessionId);
			return;
		}

		this.log.info("Message received. [sessionId={},command={}]", this.sessionId, msg.frame.getCommand());

		try {
			switch (msg.frame.getCommand()) {
			case SEND:
				getSession(msg.frame).send(msg.frame);
				break;
			case ACK: {
				final String id = msg.frame.getFirstHeader(Headers.ID);
				javax.jms.Message message = this.ackMessages.remove(id);
				if (message == null) {
					throw new IllegalStateException("No such message! [" + id + "]");
				}
				message.acknowledge();
				break;
			}
			case BEGIN: {
				if (this.txSessions.containsKey(msg.frame.getTransaction())) {
					throw new IllegalStateException("Transaction already started! [" + msg.frame.getTransaction() + "]");
				}
				final Session txSession = new Session(this, getDelegate().createSession(true, javax.jms.Session.SESSION_TRANSACTED));
				this.txSessions.put(msg.frame.getTransaction(), txSession);
				break;
			}
			case COMMIT: {
				final Session txSession = this.txSessions.remove(msg.frame.getTransaction());
				txSession.getDelegate().commit();
				break;
			}
			case ABORT: {
				final Session txSession = this.txSessions.remove(msg.frame.getTransaction());
				txSession.getDelegate().rollback();
				break;
			}
			case SUBSCRIBE: {
				final String subscriptionId = msg.frame.getFirstHeader(Headers.ID);
				final Subscription subscription = this.subscriptions.putIfAbsent(
						subscriptionId, new Subscription(getSession(msg.frame), subscriptionId, msg.frame));
				if (subscription != null)
					throw new IllegalStateException("Subscription already exists! [" + subscriptionId + "]");
				break;
			}
			case UNSUBSCRIBE: {
				final String subscriptionId = msg.frame.getFirstHeader(Headers.ID);
				final Subscription subscription = this.subscriptions.remove(subscriptionId);
				if (subscription == null)
					throw new IllegalStateException("Subscription does not exist! [" + subscriptionId + "]");
				subscription.close();
				break;
			}
			case DISCONNECT:
				// only here to short-cut potential receipt sending
				break;
			default:
				throw new IllegalArgumentException("Unexpected frame! [" + msg.frame.getCommand());
			}
			sendReceipt(msg.frame);
		} catch (JMSException e) {
			this.log.error("Error handling message! [sessionId={},command={}]", this.sessionId, msg.frame.getCommand());
		}
	}

	/**
	 * 
	 * @param frame
	 * @throws Exception
	 */
	private void sendReceipt(Frame frame)  {
		final String receiptId = frame.getFirstHeader(Headers.RECIEPT);
		if (receiptId != null) {
			send(Frame.receipt(receiptId).build());
		}
	}

	/**
	 * 
	 * @param msg
	 * @throws JMSException 
	 */
	public void addAckMessage(javax.jms.Message msg) throws JMSException {
		this.ackMessages.put(msg.getJMSMessageID(), msg);
	}

	@Override
	public void close(CloseReason reason) throws IOException {
		super.close(reason);
		this.heartBeatMonitor.close();
	}
}
