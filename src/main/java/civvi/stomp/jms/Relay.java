package civvi.stomp.jms;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.websocket.Session;

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.api.provider.DependentProvider;
import org.slf4j.Logger;

import civvi.messaging.annotation.FromBroker;
import civvi.messaging.annotation.FromClient;
import civvi.messaging.annotation.OnClose;
import civvi.messaging.event.Message;

/**
 * STOMP broker relay to JMS.
 * 
 * @author Daniel Siviter
 * @since v1.0 [19 Jul 2016]
 */
@ApplicationScoped
public class Relay {
	private final Map<String, DependentProvider<Connection>> sessions = new ConcurrentHashMap<>();

	@Inject
	private Logger log;
	@Inject
	private BeanManager manager;
	@Inject @FromBroker
	private Event<Message> messageEvent;

	/**
	 * 
	 * @param msg
	 */
	public void message(@Observes @FromClient Message msg) {
		try {
			if (msg.frame.getCommand() != null) {
				switch (msg.frame.getCommand()) {
				case CONNECT:
				case STOMP:
					this.log.info("CONNECT/STOMP recieved. Opening connection to broker. [sessionId={}]", msg.sessionId);
					final DependentProvider<Connection> conn = BeanProvider.getDependent(this.manager, Connection.class);
					conn.get().connect(msg);
					this.sessions.put(msg.sessionId, conn);
					return;
				case DISCONNECT:
					this.log.info("DISCONNECT recieved. Closing connection to broker. [sessionId={}]", msg.sessionId);
					close(msg.sessionId);
					return;
				default:
					break;
				}
			}
			this.sessions.get(msg.sessionId).get().on(msg);
		} catch (JMSException | RuntimeException e) {
			this.log.error("Unable to process message! [sessionId={},command={}]", msg.sessionId, msg.frame.getCommand(), e);
		}
	}

	/**
	 * 
	 * @param msg
	 */
	private void close(String sessionId) {
		this.sessions.computeIfPresent(sessionId, (k, v) -> {
			log.info("Destroying JMS connection. [{}]", k);
			v.destroy();
			return null;
		});
	}

	/**
	 * 
	 * @param msg
	 */
	public void close(@Observes @OnClose Session session) {
		close(session.getId());
	}

	/**
	 * 
	 * @param msg
	 */
	public void send(Message msg) {
		this.messageEvent.fire(msg);
	}
}