package cito.stomp.server.scope;

import javax.annotation.Priority;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import cito.stomp.server.Extension;
import cito.stomp.server.SessionRegistry;
import cito.stomp.server.annotation.ActivateWebSocketContext;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [27 Nov 2016]
 */
@Interceptor
@ActivateWebSocketContext
@Priority(Interceptor.Priority.LIBRARY_BEFORE + 100)
public class ActivateWebSocketContextInterceptor {

	@Inject
	private SessionRegistry registry;

	@Inject
	private BeanManager beanManager;

	@AroundInvoke
	Object invoke(InvocationContext ctx) throws Exception {
		final WebSocketContext webSocketCtx = Extension.getWebSocketContext(this.beanManager);
//		webSocketCtx.ge

		if (webSocketCtx.isActive()) {
			return ctx.proceed();
		} else {
			//            try {
			//                requestContext.activate();
			return ctx.proceed();
			//            } finally {
			//                requestContext.invalidate();
			//                requestContext.deactivate();
			//            }
		}
	}
}
