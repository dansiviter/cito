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
package cito.server;

import static cito.server.SecurityContext.NOOP;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cito.annotation.WebSocketScope;

/**
 * A producer for accessing the {@link SecurityContext} from the {@link Session}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [27 Oct 2016]
 */
@ApplicationScoped
public class SecurityContextProducer {
	private static final Logger LOG = LoggerFactory.getLogger(SecurityContextProducer.class);

	private static final String KEY = SecurityContext.class.getName();

	/**
	 * 
	 * @return
	 */
	@Produces @WebSocketScope
	public static SecurityContext securityCtx(Session session) {
		final Map<String, Object> props = session.getUserProperties();
		final SecurityContext securityCtx = (SecurityContext) props.get(KEY);
		return securityCtx != null ? securityCtx : NOOP;
	}

	/**
	 * 
	 * @param securityCtx
	 * @return
	 */
	public static void set(Session session, SecurityContext securityCtx) {
		LOG.debug("Setting session. [{}]", session != null ? session.getId() : null);
		final SecurityContext old = securityCtx(session);
		if (old != NOOP) {
			throw new IllegalStateException("Already set!");
		}
		if (securityCtx != null) {
			session.getUserProperties().put(KEY, securityCtx);
		}
	}
}
