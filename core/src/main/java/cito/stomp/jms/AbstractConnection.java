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

import javax.annotation.Nonnull;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;

import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.slf4j.Logger;

import cito.event.Message;
import cito.util.ToStringBuilder;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [2 Aug 2016]
 */
public abstract class AbstractConnection implements cito.stomp.Connection {
	@Inject
	protected Logger log;
	@Inject
	protected BeanManager beanManager;
	@Inject
	protected Relay relay;
	@Inject
	protected ConnectionFactory connectionFactory;
	@Inject
	protected Factory factory;

	@ConfigProperty(name = "cito.guest.login", defaultValue = "guest")
	private String guestLogin;
	@ConfigProperty(name = "cito.guest.password", defaultValue = "guest")
	private String guestPasscode;

	private javax.jms.Connection delegate;

	/**
	 * 
	 * @param msg
	 */
	public abstract void on(@Nonnull Message msg);

	/**
	 * 
	 * @param login
	 * @param passcode
	 * @throws JMSException 
	 */
	protected void createDelegate(String login, String passcode) throws JMSException {
		if (this.delegate != null) {
			throw new IllegalStateException("Already connected!");
		}
		if (login != null) {
			this.delegate = this.connectionFactory.createConnection(login, passcode);
		} else {
			this.delegate = this.connectionFactory.createConnection(this.guestLogin, this.guestPasscode);
		}
		final String sessionId = getSessionId();
		this.log.info("Starting JMS connection... [sessionId={}]", sessionId);
		this.delegate.setClientID(sessionId);
		this.delegate.start();
	}

	public javax.jms.Connection getDelegate() {
		return delegate;
	}

	/**
	 * 
	 */
	@PreDestroy
	public void close() {
		try {
			close(new CloseReason(CloseCodes.NORMAL_CLOSURE, null));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void close(CloseReason reason) throws IOException {
		this.log.info("Closing connection. [sessionId={},code={},reason={}]", getSessionId(), reason.getCloseCode().getCode(), reason.getReasonPhrase());
		try {
			if (this.delegate != null) {
				this.delegate.close();
			}
		} catch (JMSException e) {
			throw new IOException(e);
		}
	}

	@Override
	public String toString() {
		return ToStringBuilder
				.create(this)
				.append("sessionId", getSessionId())
				.toString();
	}
}
