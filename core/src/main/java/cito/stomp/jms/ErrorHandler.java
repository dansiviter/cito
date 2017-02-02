package cito.stomp.jms;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import cito.annotation.FromServer;
import cito.event.MessageEvent;
import cito.stomp.Frame;
import cito.stomp.Frame.Builder;
import cito.stomp.Headers;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [1 Sep 2016]
 */
@ApplicationScoped
public class ErrorHandler {
	@Inject
	private Logger log;
	@Inject @FromServer
	protected Event<MessageEvent> messageEvent;

	/**
	 * 
	 * @param relay
	 * @param sessionId
	 * @param cause
	 * @param e
	 */
	public void onError(Relay relay, String sessionId, Frame cause, String msg, Exception e) {
		this.log.warn("Error while processing frame! [sessionId={},frame.command={}]", sessionId, cause.getCommand(), e);
		final Builder error = Frame.error();
		if (cause != null && cause.containsHeader(Headers.RECIEPT)) {
			error.recieptId(cause.receipt());
		}
		if (msg == null) {
			msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
		}
		error.body(MediaType.TEXT_PLAIN_TYPE, msg);
		this.messageEvent.fire(new MessageEvent(sessionId, error.build()));
		relay.close(sessionId);
	}
}
