package civvi.messaging;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import civvi.messaging.event.Message;

/**
 * Either extend or inject this class where you wish to use it.
 * 
 * @author Daniel Siviter
 * @since v1.0 [27 Jul 2016]
 */
public abstract class Support {
	@Inject
	private Event<Message> msgEvent;


	// --- Static Methods ---

	/**
	 * 
	 * @return
	 */
	@Produces @Dependent
	public static Support support() {
		return new Support() { };
	}
}
