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
package cito.jms;

import javax.annotation.PreDestroy;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;

import org.slf4j.Logger;

/**
 * @author Daniel Siviter
 * @since v1.0 [28 Apr 2017]
 */
public abstract class JmsContextHelper {
	@Inject
	protected Logger log;
	@Inject
	private Instance<JMSContext> ctxProvider;

	private JMSContext ctx;

	/**
	 * Connect to the broker to source events.
	 */
	protected void connect() {
		this.log.info("Connecting to broker for sourcing destination events.");
		this.ctx = this.ctxProvider.get();
		this.ctx.setExceptionListener(this::onError);
	}

	/**
	 * Handles errors from the broker. As we can't guarantee that we're left in an inconsistent state we'll re-attempt
	 * connection.
	 * 
	 * @param e
	 */
	private void onError(JMSException e) {
		this.log.error("Error occured processing destination events! Reconnecting...", e);
		this.ctxProvider.destroy(this.ctx);
		connect();
	}

	/**
	 * 
	 * @return
	 */
	protected synchronized JMSContext getContext() {
		if (this.ctx == null) {
			connect();
		}
		return this.ctx;
	}

	@PreDestroy
	public void destroy() {
		if (this.ctx != null) {
			this.ctxProvider.destroy(this.ctx);
		}
	}


	// --- Inner Classes 

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [28 Apr 2017]
	 * @param <R>
	 */
	@FunctionalInterface
	private interface ContextFunction<R> {
		/**
		 * Applies this function to the given argument.
		 *
		 * @param c the context
		 * @return the function result
		 */
		R apply(JMSContext ctx) throws JMSException;
	}
}
