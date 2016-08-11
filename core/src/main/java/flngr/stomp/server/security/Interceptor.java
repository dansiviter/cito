package flngr.stomp.server.security;

import java.security.Principal;

import javax.annotation.Resource;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [8 Aug 2016]
 */
@javax.interceptor.Interceptor
public class Interceptor {
	@Resource
	private Principal principal;

	@AroundInvoke
	public Object isPermitted(InvocationContext ctx) throws Exception {
		return ctx.proceed();
	}
}
