package cito.sockjs;

import java.io.IOException;

import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [4 Jan 2017]
 */
public class EchoEndpoint extends Endpoint {
	private static final Logger LOG = LoggerFactory.getLogger(EchoEndpoint.class);

	@Override
	public void onOpen(Session session, EndpointConfig config) {
		final RemoteEndpoint.Basic remote = session.getBasicRemote();
		session.addMessageHandler(new MessageHandler.Whole<String>() {

			@Override
			public void onMessage(String message) {
				try {
					remote.sendText(message);
				} catch (IOException e) {
					LOG.error("Unable to echo!", e);
				}
			}
		});
	}


}
