package civvi.stomp.jms;

import civvi.messaging.event.Message;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [21 Jul 2016]
 */
public interface Sender {
	/**
	 * 
	 * @param msg
	 */
	void send(Message msg);

}
