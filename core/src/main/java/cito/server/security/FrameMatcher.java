package cito.server.security;

import cito.stomp.Frame;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [19 Oct 2016]
 */
@FunctionalInterface
public interface FrameMatcher {
	/**
	 * 
	 * @param frame
	 * @return
	 */
	boolean matches(Frame frame);
}
