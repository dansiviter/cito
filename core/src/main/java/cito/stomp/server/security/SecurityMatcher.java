package cito.stomp.server.security;

import cito.stomp.server.SecurityContext;

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
	 * @throws SecurityViolationException
	 */
	void isPermitted(SecurityContext ctx) throws SecurityViolationException;
}
