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

import static cito.Strings.isBlank;
import static cito.Util.isNullOrEmpty;
import static cito.stomp.Headers.ACCEPT_VERSION;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.jms.JMSException;
import javax.security.auth.login.LoginException;
import javax.websocket.CloseReason;
import javax.ws.rs.core.MediaType;

import org.apache.deltaspike.core.api.provider.BeanProvider;

import cito.annotation.FromBroker;
import cito.event.Message;
import cito.server.JaasSecurityContext;
import cito.server.SecurityContext;
import cito.server.SecurityContextProducer;
import cito.stomp.Command;
import cito.stomp.Frame;
import cito.stomp.Frame.HeartBeat;
import cito.stomp.Headers;
import cito.stomp.HeartBeatMonitor;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [21 Jul 2016]
 */
@Dependent
public class Connection extends AbstractConnection {
	private static final String[] SUPPORTED_VERSIONS = { "1.0", "1.1", "1.2" };
	private static final int HEARTBEAT_READ_DEFAULT = 10_000, HEARTBEAT_WRITE_DEFAULT = 10_000;

	private final Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();
	private final Map<String, Session> txSessions = new ConcurrentHashMap<>();
	private final Map<String, javax.jms.Message> ackMessages = new ConcurrentHashMap<>();

	@Inject // XXX use ManagedScheduledExecutorService?
	private ScheduledExecutorService scheduler;
	@Inject @FromBroker
	private Event<Message> brokerMessageEvent;
	@Inject
	private Provider<javax.websocket.Session> wsSession;

	private HeartBeatMonitor heartBeatMonitor;
	private String sessionId;
	private Session session, ackSession;

	/**
	 * Initialise the connection.
	 */
	@PostConstruct
	public void init() {
		this.heartBeatMonitor = new HeartBeatMonitor(this, this.scheduler);
	}

	@Override
	public String getSessionId() {
		return sessionId;
	}

	@Override
	public void sendToClient(@Nonnull Frame frame) {
		this.heartBeatMonitor.resetSend();
		final Command command = frame.getCommand();
		this.log.info("Sending message to client. [sessionId={},command={}]",
				this.sessionId, command != null ? command : "HEARTBEAT");
		this.brokerMessageEvent.fire(new Message(this.sessionId, frame));
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
				this.ackSession = this.factory.toSession(this, false, javax.jms.Session.CLIENT_ACKNOWLEDGE);
			}
			return this.ackSession;
		}
		if (this.session == null) {
			this.session = this.factory.toSession(this, false, javax.jms.Session.AUTO_ACKNOWLEDGE);
		}
		return this.session;
	}

	/**
	 * 
	 * @param in
	 * @return
	 * @throws JMSException
	 */
	private Session getSession(@Nonnull Frame in) throws JMSException {
		final String tx = in.transaction();
		if (tx != null)
			return this.txSessions.get(tx);

		final String ackMode = in.getFirstHeader(Headers.ACK);
		return getSession("client".equalsIgnoreCase(ackMode));
	}

	/**
	 * 
	 * @param msg
	 * @throws JMSException
	 * @throws LoginException 
	 */
	public void connect(@Nonnull Message msg) throws JMSException, LoginException {
		if (this.sessionId != null) {
			throw new IllegalStateException("Already connected!");
		}

		if (isNullOrEmpty(msg.sessionId())) {
			throw new IllegalArgumentException("Session ID cannot be null!");
		}
		this.sessionId = msg.sessionId();
	
		this.log.info("Connecting... [sessionId={}]", sessionId);
	
		String version = null;
		final Collection<String> clientSupportedVersion = Arrays.asList(msg.frame().getFirstHeader(ACCEPT_VERSION).split(","));
		for (int i = SUPPORTED_VERSIONS.length - 1; i >= 0; i--) {
			if (clientSupportedVersion.contains(SUPPORTED_VERSIONS[i])) {
				version = SUPPORTED_VERSIONS[i];
				break;
			}
		}

		if (version == null) {
			final Frame.Builder error = Frame.error().version(SUPPORTED_VERSIONS);
			error.body(MediaType.TEXT_PLAIN_TYPE, "Only STOMP v1.2 supported!");
			sendToClient(error.build());
			throw new IllegalStateException("Only STOMP v1.2 supported!" + msg.frame().getHeaders(Headers.ACCEPT_VERSION));
		}

		final Frame.Builder connected = Frame.connnected(version, this.sessionId, "localhost");

		final HeartBeat heartBeat = msg.frame().heartBeat();
		if (!version.equals("1.0") && heartBeat != null) {
			connected.heartbeat(HEARTBEAT_READ_DEFAULT, HEARTBEAT_WRITE_DEFAULT);
		}

		String login = msg.frame().getFirstHeader(Headers.LOGIN);
		final String passcode = msg.frame().getFirstHeader(Headers.PASSCODE);

		SecurityContext securityCtx = SecurityContextProducer.securityCtx(this.wsSession.get());
		if (isBlank(login) && securityCtx != null && securityCtx.getUserPrincipal() != null) {
			login = securityCtx.getUserPrincipal().getName();
		} else if (!isBlank(login) && securityCtx == null) { // perform login if security context doesn't already exist
			final JaasSecurityContext jaasSecurityContext = BeanProvider.getDependent(this.beanManager, JaasSecurityContext.class).get();
			jaasSecurityContext.login(login, passcode.toCharArray());
			SecurityContextProducer.set(this.wsSession.get(), jaasSecurityContext);
		}

		createDelegate(login, passcode);

		sendToClient(connected.build());

		if (!version.equals("1.0") && heartBeat != null) {
			final long readDelay = Math.max(heartBeat.x, HEARTBEAT_READ_DEFAULT);
			final long writeDelay = Math.max(HEARTBEAT_WRITE_DEFAULT, heartBeat.y);
			this.heartBeatMonitor.start(readDelay, writeDelay);
		}
	}

	/**
	 * 
	 * @param msg
	 */
	@Override
	public void on(Message msg) {
		if (!getSessionId().equals(msg.sessionId())) {
			throw new IllegalArgumentException("Session identifier mismatch! [expected=" + this.sessionId + ",actual=" + msg.sessionId() + "]");
		}
		if (msg.frame().getCommand() == Command.CONNECT || msg.frame().getCommand() == Command.DISCONNECT) {
			throw new IllegalArgumentException(msg.frame().getCommand() + " not supported! [" + msg.sessionId() + "]");
		}

		this.heartBeatMonitor.resetRead();

		if (msg.frame().isHeartBeat()) {
			this.log.debug("Heartbeat recieved. [sessionId={}]", this.sessionId);
			return;
		}

		this.log.info("Message received. [sessionId={},command={}]", this.sessionId, msg.frame().getCommand());

		try {
			switch (msg.frame().getCommand()) {
			case SEND:
				getSession(msg.frame()).sendToBroker(msg.frame());
				break;
			case ACK: {
				final String id = msg.frame().getFirstHeader(Headers.ID);
				javax.jms.Message message = this.ackMessages.remove(id);
				if (message == null) {
					throw new IllegalStateException("No such message to ACK! [" + id + "]");
				}
				message.acknowledge();
				break;
			}
			case NACK: {
				final String id = msg.frame().getFirstHeader(Headers.ID);
				javax.jms.Message message = this.ackMessages.remove(id);
				if (message == null) {
					throw new IllegalStateException("No such message to NACK! [" + id + "]");
				}
				// not sure what to do here, but we're 
				this.log.warn("NACK recieved, but no JMS equivalent! [{}]", id);
				break;
			}
			case BEGIN: {
				if (this.txSessions.containsKey(msg.frame().transaction())) {
					throw new IllegalStateException("Transaction already started! [" + msg.frame().transaction() + "]");
				}
				final Session txSession = this.factory.toSession(this, true, javax.jms.Session.SESSION_TRANSACTED);
				this.txSessions.put(msg.frame().transaction(), txSession);
				break;
			}
			case COMMIT: {
				final String tx = msg.frame().transaction();
				final Session txSession = this.txSessions.remove(tx);
				if (txSession == null) {
					throw new IllegalStateException("Transaction session does not exists! [" + tx + "]");
				}
				txSession.commit();
				break;
			}
			case ABORT: {
				final String tx = msg.frame().transaction();
				final Session txSession = this.txSessions.remove(tx);
				if (txSession == null) {
					throw new IllegalStateException("Transaction session does not exists! [" + tx + "]");
				}
				txSession.rollback();
				break;
			}
			case SUBSCRIBE: {
				final String subscriptionId = msg.frame().getFirstHeader(Headers.ID);
				this.subscriptions.compute(
						subscriptionId,
						(k, v) -> { 
							try {
								if (v != null) {
									throw new IllegalStateException("Subscription already exists! [" + subscriptionId + "]");
								}
								return new Subscription(getSession(msg.frame()), k, msg.frame());
							} catch (JMSException e) {
								throw new IllegalStateException("Unable to subscribe! [" + subscriptionId + "]");
							}
						});
				break;
			}
			case UNSUBSCRIBE: {
				final String subscriptionId = msg.frame().getFirstHeader(Headers.ID);
				final Subscription subscription = this.subscriptions.remove(subscriptionId);
				if (subscription == null)
					throw new IllegalStateException("Subscription does not exist! [" + subscriptionId + "]");
				subscription.close();
				break;
			}
			case ERROR: {
				sendToClient(msg.frame());
				break;
			}
			default:
				throw new IllegalArgumentException("Unexpected frame! [" + msg.frame().getCommand());
			}
			sendReceipt(msg.frame());
		} catch (JMSException e) {
			this.log.error("Error handling message! [sessionId={},command={}]", this.sessionId, msg.frame().getCommand(), e);
		}
	}

	/**
	 * 
	 * @param msg
	 */
	public void disconnect(@Nonnull Message msg) {
		sendReceipt(msg.frame());
	}

	/**
	 * 
	 * @param frame
	 * @throws Exception
	 */
	private void sendReceipt(@Nonnull Frame frame)  {
		final String receiptId = frame.getFirstHeader(Headers.RECIEPT);
		if (receiptId != null) {
			sendToClient(Frame.receipt(receiptId).build());
		}
	}

	/**
	 * 
	 * @param msg
	 * @throws JMSException 
	 */
	public void addAckMessage(@Nonnull javax.jms.Message msg) throws JMSException {
		this.ackMessages.put(msg.getJMSMessageID(), msg);
	}

	@Override
	public void close(CloseReason reason) throws IOException {
		super.close(reason);
		this.heartBeatMonitor.close();
	}
}
