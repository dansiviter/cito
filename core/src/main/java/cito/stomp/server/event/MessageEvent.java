package cito.stomp.server.event;

import java.lang.reflect.Type;

import cito.stomp.Frame;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [19 Jul 2016]
 */
public interface MessageEvent {
	/**
	 * @return the originating session identifier. If this is a internally generated message (i.e. application code)
	 * then this will be {@code null}.
	 */
	String sessionId();

	/**
	 * @return the STOMP frame.
	 */
	Frame frame();

	/**
	 * 
	 * 
	 * @param type
	 * @return
	 */
	default <T> T getBean(Class<T> type) {
		return getBean((Type) type);
	}

	/**
	 * 
	 * 
	 * @param type
	 * @return
	 */
	default <T> T getBean(Type type) {
		throw new UnsupportedOperationException();
	}
}
