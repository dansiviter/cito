package cito.stomp.server.security;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [27 Oct 2016]
 */
public class SecurityViolationException extends Exception {
	private static final long serialVersionUID = 7037521140753562863L;

	public SecurityViolationException(String msg) {
		super(msg);
	}
}
