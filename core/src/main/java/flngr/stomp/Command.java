package flngr.stomp;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [12 Jul 2016]
 */
public enum Command {
	ABORT,
	ACK,
	BEGIN,
	COMMIT,
	CONNECT,
	CONNECTED,
	DISCONNECT,
	ERROR,
	MESSAGE,
	NACK,
	RECIEPT,
	SEND,
	STOMP,
	SUBSCRIBE,
	UNSUBSCRIBE;

	/**
	 * 
	 * @return
	 */
	public boolean server() {
		return this == CONNECTED || this == ERROR || this == MESSAGE || this == RECIEPT;
	}

	/**
	 * 
	 * @return
	 */
	public boolean destination() {
		return this == MESSAGE || this == SEND || this == SUBSCRIBE;
	}

	/**
	 * 
	 * @return
	 */
	public boolean subscriptionId() {
		return this == MESSAGE || this == SUBSCRIBE || this == UNSUBSCRIBE;
	}

	/**
	 * 
	 * @return
	 */
	public boolean body() {
		return this == SEND || this == MESSAGE || this == ERROR; 
	}

	/**
	 * 
	 * @return
	 */
	public boolean transaction() {
		return this == BEGIN || this == COMMIT || this == ABORT; 
	}
}
