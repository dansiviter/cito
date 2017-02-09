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
package cito.scope;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cito.annotation.WebSocketScope;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [17 Aug 2016]
 */
@ApplicationScoped
public class WebSocketSessionHolder {
	private static final Logger LOG = LoggerFactory.getLogger(WebSocketSessionHolder.class);

	private final ThreadLocal<Session> session = new ThreadLocal<>();

	/**
	 * 
	 * @param session
	 */
	public void set(Session session) {
		LOG.debug("Setting session. [sessionId={}]", session.getId());
		if (this.session.get() != null) {
			throw new IllegalStateException("Session already set! [expected=" + this.session.get().getId() + ",current=" + session.getId() + "]");
		}
		this.session.set(session);
	}

	/**
	 * 
	 */
	public void remove() {
		final Session session = this.session.get();
		if (session == null) {
			throw new IllegalArgumentException("Session not set!");
		}
		LOG.debug("Removing session. [sessionId={}]", session.getId());
		this.session.remove();
	}

	/**
	 * 
	 * @return
	 */
	@Produces @WebSocketScope
	public Session get() {
		return this.session.get();
	}
}
