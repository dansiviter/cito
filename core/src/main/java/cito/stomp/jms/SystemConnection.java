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

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSException;

import cito.event.MessageEvent;
import cito.stomp.Frame;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [2 Aug 2016]
 */
@ApplicationScoped
public class SystemConnection extends AbstractConnection {
	static final String SESSION_ID = "$Y$TEM";

	@Inject
	private Factory factory;

	private Session session;

	@Override
	public String getSessionId() {
		return SESSION_ID;
	}

	/**
	 * 
	 */
	@PostConstruct
	public void init() {
		try {
			createDelegate(null, null);
		} catch (JMSException e) {
			throw new IllegalStateException("Unable to create system connection!", e);
		}
	}

	@Override
	public void sendToClient(Frame frame) throws IOException {
		throw new UnsupportedOperationException("Cannot sent to client from system connection!");
	}

	/**
	 * 
	 * @return
	 * @throws JMSException
	 */
	private Session getSession() throws JMSException {
		if (this.session == null) {
			this.session = factory.toSession(this, false, javax.jms.Session.AUTO_ACKNOWLEDGE);
		}
		return this.session;
	}

	@Override
	public void on(MessageEvent msg) {
		final String sessionId = msg.sessionId();
		if (!getSessionId().equals(sessionId) && sessionId != null) {
			throw new IllegalArgumentException("Session identifier mismatch! [expected=" + getSessionId() + " OR null,actual=" + msg.sessionId() + "]");
		}

		try {
			getSession().sendToBroker(msg.frame());
		} catch (JMSException e) {
			this.log.error("Error handling message! [sessionId={},command={}]", getSessionId(), msg.frame().getCommand(), e);
		}
	}
}
