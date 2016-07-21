package civvi.stomp.jms;

import javax.jms.Destination;
import javax.jms.JMSException;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [21 Jul 2016]
 */
public class Session {
	private final javax.jms.Session delegate;

	Session(javax.jms.Session delegate) {
		this.delegate = delegate;
	}

	/**
	 * 
	 * @param session
	 * @param name
	 * @return
	 * @throws JMSException
	 */
	private Destination toDestination(String name) throws JMSException {
		final int separatorIndex = name.indexOf('/', 1);
		final String type = name.substring(0, separatorIndex);   
		name = name.substring(separatorIndex + 1, name.length());
		switch (type) {
		case "/queue/":
			return this.delegate.createQueue(name);
		case "/topic/":
			return this.delegate.createTopic(name);
		default:
			throw new IllegalArgumentException("Unknown destination! [type=" + type + ",name="  + name + "]");

		}
	}
}
