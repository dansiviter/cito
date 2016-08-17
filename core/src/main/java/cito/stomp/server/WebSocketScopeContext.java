package cito.stomp.server;

import java.lang.annotation.Annotation;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.inject.spi.BeanManager;
import javax.websocket.Session;

import org.apache.deltaspike.core.impl.scope.AbstractBeanHolder;
import org.apache.deltaspike.core.util.context.AbstractContext;
import org.apache.deltaspike.core.util.context.ContextualStorage;

import cito.QuietClosable;
import cito.stomp.server.annotation.WebSocketScope;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [17 Aug 2016]
 */
public class WebSocketScopeContext extends AbstractContext {
	private final ThreadLocal<Session> session = new ThreadLocal<>();
	private final Holder holder = new Holder();
	
	private final BeanManager beanManager;

	protected WebSocketScopeContext(BeanManager beanManager) {
		super(beanManager);
		this.beanManager = beanManager;
	}

	@Override
	public Class<? extends Annotation> getScope() {
		return WebSocketScope.class;
	}

	@Override
	public boolean isActive() {
		return true;
	}

	/**
	 * 
	 * @param session
	 * @return
	 */
	public QuietClosable activate(Session session) {
		if (this.session.get() != null) {
			throw new IllegalStateException("Session already set!");
		}
		this.session.set(session);

		final Thread thread = Thread.currentThread();
		return new QuietClosable() {
			@Override
			public void close() {
				if (Thread.currentThread() != thread) {
					throw new IllegalStateException("Different thread! Potential resource leak!");
				}
				WebSocketScopeContext.this.session.remove();
			}
		};
	}

	@Override
	protected ContextualStorage getContextualStorage(Contextual<?> contextual, boolean createIfNotExist) {
		final Session session = this.session.get();
		if (session == null) {
			throw new IllegalStateException("No session available!");
		}
		return holder.getContextualStorage(this.beanManager, session.getId(), createIfNotExist);
	}


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [17 Aug 2016]
	 */
	private static class Holder extends AbstractBeanHolder<String> {

	}
}
