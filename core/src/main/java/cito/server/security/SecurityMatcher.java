package cito.server.security;

import cito.server.SecurityContext;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [19 Oct 2016]
 */
@FunctionalInterface
public interface SecurityMatcher {
	/**
	 * 
	 * @param ctx
	 * @return
	 */
	boolean isPermitted(SecurityContext ctx);
}
