package cito;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [3 Jan 2018]
 *
 */
public enum DestinationType {
	/** JMS Topic destination */
	TOPIC,
	/** JMS Queue destination */
	QUEUE,
	/** Destination type that's only handled in the app layer **/
	DIRECT;

	/**
	 * 
	 * @param destination
	 * @return
	 */
	public static DestinationType from(String destination) {
		final String type = destination.substring(0, destination.indexOf('/', 1) + 1).toLowerCase();
		switch (type) {
		case "queue/":
			return QUEUE;
		case "topic/":
			return TOPIC;
		default:
			return DIRECT;
		}
	}
}
