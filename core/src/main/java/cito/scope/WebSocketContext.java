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

import static cito.cdi.Util.injectFields;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.websocket.Session;

import org.apache.deltaspike.core.impl.scope.AbstractBeanHolder;
import org.apache.deltaspike.core.util.context.AbstractContext;
import org.apache.deltaspike.core.util.context.ContextualStorage;
import org.slf4j.Logger;

import cito.QuietClosable;
import cito.annotation.WebSocketScope;
import cito.server.SessionRegistry;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [17 Aug 2016]
 */
public class WebSocketContext extends AbstractContext {
	private final Holder holder;

	private final BeanManager beanManager;
	private WebSocketSessionHolder sessionHolder;

	@Inject
	private Logger log;
	@Inject
	private Provider<SessionRegistry> sessionRegistry;

	public WebSocketContext(BeanManager beanManager) {
		super(beanManager);
		this.holder = new Holder(isPassivatingScope());
		this.beanManager = beanManager;
		injectFields(beanManager, this);
	}

	public void init(WebSocketSessionHolder sessionHolder) {
		this.sessionHolder = sessionHolder;
	}

	@Override
	public Class<? extends Annotation> getScope() {
		return WebSocketScope.class;
	}

	@Override
	public boolean isActive() {
		return true; // active regardless
	}

	/**
	 * 
	 * @param session
	 * @return
	 */
	public QuietClosable activate(Session session) {
		this.log.debug("Activiating scope. [sessionId={}]", session.getId());
		this.sessionHolder.set(session);

		final Thread thread = Thread.currentThread();
		return () -> {
			if (Thread.currentThread() != thread) {
				throw new IllegalStateException("Different thread! Potential resource leak!");
			}
			WebSocketContext.this.sessionHolder.remove();
		};
	}

	/**
	 * @return the current session held within the context.
	 */
	public Session currentSession() {
		return this.sessionHolder.get();
	}

	/**
	 * 
	 * @param contextual
	 * @return
	 */
	private Session getSession(Contextual<?> contextual) {
		final List<Session> sessions = new ArrayList<>();
		for (Entry<String, ContextualStorage> e : this.holder.getStorageMap().entrySet()) {
			final Object key = e.getValue().getBeanKey(contextual);
			if (e.getValue().getBean(key) != null) {
				final Optional<Session> session = this.sessionRegistry.get().getSession(e.getKey());
				if (session.isPresent()) {
					sessions.add(session.get());
				}
			}
		}
		if (sessions.size() > 1) {
			throw new IllegalStateException("Too many sessions! [" + contextual + "]");
		}
		return sessions.isEmpty() ? null : sessions.get(0);
	}

	/**
	 * Disposes all beans associated with the {@link Session}.
	 * 
	 * @param session
	 */
	public void dispose(Session session) {
		this.log.debug("Disposing scope. [sessionId={}]", session.getId());
		try (QuietClosable c = activate(session)) {
			final ContextualStorage storage = getContextualStorage(null, false);
			if (storage != null) {
				AbstractContext.destroyAllActive(storage);
			}
		}
	}

	@Override
	protected ContextualStorage getContextualStorage(Contextual<?> contextual, boolean createIfNotExist) {
		Session session = this.sessionHolder.get();
		// return the storage for the Contextual. This can only be achieved if the Session was seen before as the storage
		// will not contain the 
		if (session == null && !createIfNotExist) {
			session = getSession(contextual);
		}
		if (session == null) {
			throw new ContextNotActiveException("WebSocketContext: no WebSocket session set for the current Thread yet!");
		}
		return this.holder.getContextualStorage(this.beanManager, session.getId(), createIfNotExist);
	}

	@Override
	protected List<ContextualStorage> getActiveContextualStorages() {
		return new ArrayList<ContextualStorage>(this.holder.getStorageMap().values());
	}


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [17 Aug 2016]
	 */
	private static class Holder extends AbstractBeanHolder<String> {
		private static final long serialVersionUID = 8050340714947625398L;

		public Holder(boolean passivatingScope) {
			super(true, passivatingScope);
		}
	}
}
