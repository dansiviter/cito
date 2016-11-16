package cito.stomp.server.scope;

import java.lang.annotation.Annotation;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.inject.spi.BeanManager;
import javax.websocket.Session;

import org.apache.deltaspike.core.impl.scope.AbstractBeanHolder;
import org.apache.deltaspike.core.util.context.AbstractContext;
import org.apache.deltaspike.core.util.context.ContextualStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cito.QuietClosable;
import cito.stomp.server.annotation.WebSocketScope;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [17 Aug 2016]
 */
public class WebSocketContext extends AbstractContext {
	private static final Logger LOG = LogManager.getLogger(WebSocketContext.class);

	private final Holder holder = new Holder();

	private final BeanManager beanManager;
	private WebSocketSessionHolder sessionHolder;

	public WebSocketContext(BeanManager beanManager) {
		super(beanManager);
		this.beanManager = beanManager;
		LOG.info("Context initialised.");
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
		return true;
	}

	/**
	 * 
	 * @param session
	 * @return
	 */
	public QuietClosable activate(Session session) {
		if (this.sessionHolder.get() != null && session.getId().equals(this.sessionHolder.get().getId())) {
			return QuietClosable.NOOP;
		}

		LOG.debug("Activiating scope. [sessionId={}]", session.getId());
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
	 * @param session
	 */
	public void dispose(Session session) {
		LOG.debug("Disposing scope. [sessionId={}]", session.getId());
		this.sessionHolder.set(session);
		final ContextualStorage storage = getContextualStorage(null, false);
		 AbstractContext.destroyAllActive(storage);
	}

	@Override
	protected ContextualStorage getContextualStorage(Contextual<?> contextual, boolean createIfNotExist) {
		final Session session = this.sessionHolder.get();
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
		private static final long serialVersionUID = 8050340714947625398L;
	}
}
