package cito.stomp.jms;

import javax.ws.rs.core.MediaType;

import cito.stomp.Frame;
import cito.stomp.Headers;
import cito.stomp.Frame.Builder;
import cito.stomp.server.event.MessageEvent;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [1 Sep 2016]
 */
public class ErrorHandler {
	/**
	 * 
	 * @param relay
	 * @param sessionId
	 * @param cause
	 * @param e
	 */
	public void onError(Relay relay, String sessionId, Frame cause, Exception e) {
		final Builder error = Frame.error();
		if (cause != null && cause.containsHeader(Headers.RECIEPT)) {
			error.recieptId(cause.getReceipt());
		}
		error.body(MediaType.TEXT_PLAIN_TYPE, e.getMessage());
		relay.send(new MessageEvent(sessionId, error.build()));
		relay.close(sessionId);
	}
}
