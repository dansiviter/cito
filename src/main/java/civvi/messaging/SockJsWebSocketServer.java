package civvi.messaging;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.PathParam;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [12 Jul 2016]
 */
//@ServerEndpoint("/{serverId}/{sessionId}/websocket")
public class SockJsWebSocketServer extends WebSocketServer {

	
	@OnMessage
	public void onMessage(Session session, String msg, @PathParam("serverId") String serverId, @PathParam("sessionId") String sessionId) {
		System.out.println("onMessage" + session.getId() + " - " + msg);
	}
	
}
