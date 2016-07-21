package civvi.messaging;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import civvi.messaging.annotation.FromBroker;
import civvi.messaging.annotation.FromClient;
import civvi.messaging.annotation.OnClose;
import civvi.messaging.event.Message;
import civvi.stomp.jms.Connection;
import civvi.stomp.jms.Sender;

/**
 * STOMP broker relay.
 * 
 * @author Daniel Siviter
 * @since v1.0 [19 Jul 2016]
 */
@ApplicationScoped
public class JmsRelay implements Sender {
	private static final Logger LOG = LoggerFactory.getLogger(JmsRelay.class);

	private final Map<String, Connection> sessions = new ConcurrentHashMap<>();

	@Inject
	private ConnectionFactory factory;
	@Inject @FromBroker
	private Event<Message> messageEvent;

	/**
	 * 
	 * @param msg
	 */
	public void message(@Observes @FromClient Message msg) {
		try {
			switch (msg.frame.getCommand()) {
			case CONNECT:
			case STOMP:
				LOG.info("Opening connection to broker. [sessionId={}]", msg.sessionId);
				final Connection sessionConn = new Connection(this, msg.sessionId, factory).open(msg);
				this.sessions.put(msg.sessionId, sessionConn);
				break;
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param msg
	 */
	public void webSocket(@Observes @OnClose Session session) {
		LOG.info("WebSocket onClose received!");
		this.sessions.computeIfPresent(session.getId(), (k, v) -> {
			try {
				v.close();
			} catch (JMSException e) {
				LOG.warn("Unable to close session!", e); 
			}
			return null;
		});
	}

	@Override
	public void send(Message msg) {
		this.messageEvent.fire(msg);
	}
}