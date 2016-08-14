package cito.stomp.server.ws;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import cito.stomp.Frame;
import cito.stomp.FrameEncoding;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [15 Jul 2016]
 */
@ServerEndpoint(
		value = "/socket/{server}/{session}/websocket",
		subprotocols = { "v11.stomp", "v12.stomp" },
		encoders = FrameEncoding.class,
		decoders = FrameEncoding.class
)
public class WebSocketServer extends AbstractWebSocketServer {
	@OnOpen
	public void onOpen(
			@PathParam("server") String server,
			@PathParam("session") String sockJsSession,
			Session session, EndpointConfig config)
	{
		open(session, config);
	}

	@OnMessage
	public void onMessage(
			@PathParam("server") String server,
			@PathParam("session") String sockJsSession,
			Session session, Frame frame)
	{
		message(session, frame);
	}

	@OnClose
	public void onClose(
			@PathParam("server") String server,
			@PathParam("session") String sockJsSession,
			Session session, CloseReason reason)
	{
		close(session, reason);
	}

	@OnError
	public void onError(
			@PathParam("server") String server,
			@PathParam("session") String sockJsSession,
			Session session, Throwable t)
	{
		error(session, t);
	}
}
