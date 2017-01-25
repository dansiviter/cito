package cito.stomp.server.event;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

import cito.QuietClosable;

/**
 * Holds and produces only the message sent from the client.
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Jan 2017]
 */
@ApplicationScoped
public class ClientMessageEventProducer {
	private static final ThreadLocal<MessageEvent> HOLDER = new ThreadLocal<>();

	@Produces @Dependent
	public static MessageEvent get() {
		return HOLDER.get();
	}

	/**
	 * 
	 * @param e
	 */
	public static QuietClosable set(MessageEvent e) {
		final MessageEvent old = get();
		if (old != null) {
			throw new IllegalStateException("Already set!");
		}
		HOLDER.set(e);
		return new QuietClosable() {
			@Override
			public void close() {
				HOLDER.remove();
			}
		};
	}
}
