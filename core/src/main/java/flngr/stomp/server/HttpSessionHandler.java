package flngr.stomp.server;

import javax.inject.Inject;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [9 Aug 2016]
 */
@WebListener("Monitors the state of the HTTP sessions on the server.")
public class HttpSessionHandler implements HttpSessionListener {
	@Inject
	private HttpSessionRegistry registry;
	
	@Override
	public void sessionCreated(HttpSessionEvent se) {
		this.registry.register(se.getSession());
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		this.registry.unregister(se.getSession());
	}
}
