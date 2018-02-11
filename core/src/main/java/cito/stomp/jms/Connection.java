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

import static cito.Util.isNullOrEmpty;
import static cito.stomp.Header.Standard.ACCEPT_VERSION;
import static cito.stomp.Header.Standard.ACK;
import static cito.stomp.Header.Standard.LOGIN;
import static cito.stomp.Header.Standard.PASSCODE;
import static cito.stomp.Header.Standard.RECEIPT;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.jms.JMSException;
import javax.security.auth.login.LoginException;
import javax.websocket.CloseReason;
import javax.ws.rs.core.MediaType;

import cito.DestinationType;
import cito.annotation.FromBroker;
import cito.event.Message;
import cito.server.SecurityContext;
import cito.stomp.Command;
import cito.stomp.Frame;
import cito.stomp.Frame.HeartBeat;
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

	@Resource
	private ManagedScheduledExecutorService scheduler;

	@Inject @FromBroker
	private Event<Message> brokerMessageEvent;
	@Inject
	private Provider<SecurityContext> securityCtx;

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
		final Command command = frame.command();

		if (frame.isHeartBeat()) {
			this.log.debug("Sending message to client. [sessionId={},command=HEARTBEAT]", this.sessionId);
		} else {
			this.log.info("Sending message to client. [sessionId={},command={}]", this.sessionId, command);
		}
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
		final Optional<String> tx = in.transaction();
		if (tx.isPresent())
			return this.txSessions.get(tx.get());

		final String ackMode = in.getFirst(ACK).orElse(null);
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
		final Collection<String> clientSupportedVersion = Arrays.asList(msg.frame().getFirst(ACCEPT_VERSION).get().split(","));
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
			throw new IllegalStateException("Only STOMP v1.2 supported!" + msg.frame().get(ACCEPT_VERSION));
		}

		final Frame.Builder connected = Frame.connnected(version, this.sessionId, "localhost");

		final Optional<HeartBeat> heartBeat = msg.frame().heartBeat();
		if (!"1.0".equals(version) && heartBeat.isPresent()) {
			connected.heartbeat(HEARTBEAT_READ_DEFAULT, HEARTBEAT_WRITE_DEFAULT);
		}

		final String login = msg.frame().getFirst(LOGIN).orElseGet(() -> {
			final SecurityContext securityCtx = this.securityCtx.get();
			return securityCtx.getUserPrincipal() != null ? securityCtx.getUserPrincipal().getName() : null;
		});
		final String passcode = msg.frame().getFirst(PASSCODE).orElse(null);

		createDelegate(login, passcode);

		sendToClient(connected.build());

		if (!version.equals("1.0") && heartBeat.isPresent()) {
			final long readDelay = Math.max(heartBeat.get().x, HEARTBEAT_READ_DEFAULT);
			final long writeDelay = Math.max(HEARTBEAT_WRITE_DEFAULT, heartBeat.get().y);
			this.heartBeatMonitor.start(readDelay, writeDelay);
		}
	}

	@Override
	public void on(Message msg) {
		if (!getSessionId().equals(msg.sessionId())) {
			throw new IllegalArgumentException("Session identifier mismatch! [expected=" + this.sessionId + ",actual=" + msg.sessionId() + "]");
		}
		if (msg.frame().command() == Command.CONNECT || msg.frame().command() == Command.DISCONNECT) {
			throw new IllegalArgumentException(msg.frame().command() + " not supported! [" + msg.sessionId() + "]");
		}

		this.heartBeatMonitor.resetRead();

		if (msg.frame().isHeartBeat()) {
			this.log.debug("Heartbeat recieved from client. [sessionId={}]", this.sessionId);
			return;
		}

		this.log.info("Message received from client. [sessionId={},command={}]", this.sessionId, msg.frame().command());

		try {
			switch (msg.frame().command()) {
			case SEND:
				if (DestinationType.from(msg.frame().destination().get()) != DestinationType.DIRECT) {
					getSession(msg.frame()).sendToBroker(msg.frame());
				}
				break;
			case ACK: {
				final String id = msg.frame().subscription().get();
				javax.jms.Message message = this.ackMessages.remove(id);
				if (message == null) {
					throw new IllegalStateException("No such message to ACK! [" + id + "]");
				}
				message.acknowledge();
				break;
			}
			case NACK: {
				final String id = msg.frame().subscription().get();
				javax.jms.Message message = this.ackMessages.remove(id);
				if (message == null) {
					throw new IllegalStateException("No such message to NACK! [" + id + "]");
				}
				// not sure what to do here, but we're 
				this.log.warn("NACK recieved, but no JMS equivalent! [{}]", id);
				break;
			}
			case BEGIN: {
				final String tx = msg.frame().transaction().get();
				if (this.txSessions.containsKey(tx)) {
					throw new IllegalStateException("Transaction already started! [" + tx + "]");
				}
				final Session txSession = this.factory.toSession(this, true, javax.jms.Session.SESSION_TRANSACTED);
				this.txSessions.put(msg.frame().transaction().get(), txSession);
				break;
			}
			case COMMIT: {
				final String tx = msg.frame().transaction().get();
				final Session txSession = this.txSessions.remove(tx);
				if (txSession == null) {
					throw new IllegalStateException("Transaction session does not exists! [" + tx + "]");
				}
				txSession.commit();
				break;
			}
			case ABORT: {
				final String tx = msg.frame().transaction().get();
				final Session txSession = this.txSessions.remove(tx);
				if (txSession == null) {
					throw new IllegalStateException("Transaction session does not exists! [" + tx + "]");
				}
				txSession.rollback();
				break;
			}
			case SUBSCRIBE: {
				final String subscriptionId = msg.frame().subscription().get();
				this.subscriptions.compute(
						subscriptionId,
						(k, v) -> { 
							try {
								if (v != null) {
									throw new IllegalStateException("Subscription already exists! [" + subscriptionId + "]");
								}
								return new Subscription(getSession(msg.frame()), k, msg.frame());
							} catch (JMSException e) {
								throw new IllegalStateException("Unable to subscribe! [" + subscriptionId + "]", e);
							}
						});
				break;
			}
			case UNSUBSCRIBE: {
				final String subscriptionId = msg.frame().subscription().get();
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
				throw new IllegalArgumentException("Unexpected frame! [" + msg.frame().command());
			}
			sendReceipt(msg.frame());
		} catch (JMSException e) {
			this.log.error("Error handling message! [sessionId={},command={}]", this.sessionId, msg.frame().command(), e);
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
		frame.getFirst(RECEIPT).ifPresent(id -> sendToClient(Frame.receipt(id).build()));
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
