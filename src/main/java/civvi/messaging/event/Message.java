package civvi.messaging.event;

import civvi.stomp.Frame;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [19 Jul 2016]
 */
public class Message {
	public final String sessionId;
	public final Frame frame;

	public Message(String sessionId, Frame frame) {
		this.sessionId = sessionId;
		this.frame = frame;
	}
}
