package cito.broker;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

import cito.QuietClosable;
import cito.event.DestinationEvent;

/**
 * Acts as both a interface to detect if {@link DestinationEvent}s will be produced by the broker and to hold/produce
 * {@link DestinationEvent} within the CDI runtime.
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Jan 2017]
 */
public interface DestinationEventProducer {
	public static final ThreadLocal<DestinationEvent> HOLDER = new ThreadLocal<>();

	@Produces @Dependent
	public static DestinationEvent get() {
		return HOLDER.get();
	}

	/**
	 * 
	 * @param e
	 */
	public static QuietClosable set(DestinationEvent e) {
		final DestinationEvent old = get();
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