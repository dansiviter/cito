package cito.stomp.jms;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.Logger;

import cito.stomp.Frame;
import cito.stomp.Frame.Builder;
import cito.stomp.Headers;
import cito.stomp.server.event.BasicMessageEvent;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [1 Sep 2016]
 */
@ApplicationScoped
public class ErrorHandler {
	@Inject
	private Logger log;

	/**
	 * 
	 * @param relay
	 * @param sessionId
	 * @param cause
	 * @param e
	 */
	public void onError(Relay relay, String sessionId, Frame cause, Exception e) {
		this.log.warn("Error while processing frame! [sessionId={},frame.command={}]", sessionId, cause.getCommand(), e);
		final Builder error = Frame.error();
		if (cause != null && cause.containsHeader(Headers.RECIEPT)) {
			error.recieptId(cause.receipt());
		}
		error.body(MediaType.TEXT_PLAIN_TYPE, e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
		relay.send(new BasicMessageEvent(sessionId, error.build()));
		relay.close(sessionId);
	}
}
