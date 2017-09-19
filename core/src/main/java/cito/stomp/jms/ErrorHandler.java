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

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import cito.annotation.FromServer;
import cito.event.Message;
import cito.stomp.Frame;
import cito.stomp.Frame.Builder;
import cito.stomp.Header;

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
	private Event<Message> messageEvent;

	/**
	 * 
	 * @param relay
	 * @param sessionId
	 * @param cause
	 * @param msg
	 * @param e
	 */
	public void onError(@Nonnull Relay relay, @Nonnull String sessionId, @Nonnull Frame cause, String msg, Exception e) {
		this.log.warn("Error while processing frame! [sessionId={},frame.command={}]", sessionId, cause.getCommand(), e);
		final Builder error = Frame.error();
		if (cause.contains(Header.Standard.RECEIPT)) {
			error.receiptId(cause.receipt());
		}
		if (msg == null && e == null) {
			throw new IllegalStateException("Either 'msg' or 'e' must not be null!");
		}
		if (msg == null && e != null) {
			msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
		}
		error.body(MediaType.TEXT_PLAIN_TYPE, msg);
		this.messageEvent.fire(new Message(sessionId, error.build()));
		relay.close(sessionId);
	}
}
