package cito.stomp;

import java.io.IOException;

import javax.websocket.CloseReason;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Jul 2016]
 */
public interface Connection {
	/**
	 * 
	 * @return
	 */
	String getSessionId();

	/**
	 * 
	 * @param frame
	 * @throws IOException
	 */
	void sendToClient(Frame frame) throws IOException;

	/**
	 * 
	 * @param reason
	 * @throws IOException
	 */
	void close(CloseReason reason) throws IOException;
}
