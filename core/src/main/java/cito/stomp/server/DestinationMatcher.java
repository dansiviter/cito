package cito.stomp.server;

/**
 * Defines a way of testing if a {@code destination} matches the {@code test}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [7 Sep 2016]
 */
public interface DestinationMatcher {
	/**
	 * 
	 * @param test
	 * @param destination
	 * @return
	 */
	boolean matches(String test, String destination);
}
