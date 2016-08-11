package flngr;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [18 Jul 2016]
 */
public class DestinationEvent {
	private final Type type;
	private final DestinationType destinationType;
	private final String destination;

	public DestinationEvent(Type type, DestinationType destinationType, String destination) {
		this.type = type;
		this.destinationType = destinationType;
		this.destination = destination;
	}

	public Type getType() {
		return type;
	}

	public DestinationType getDestinationType() {
		return destinationType;
	}

	public String getDestination() {
		return destination;
	}


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [19 Jul 2016]
	 */
	public enum Type {
		ADDED,
		REMOVED
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [19 Jul 2016]
	 */
	public enum DestinationType {
		TOPIC,
		QUEUE
	}
}
