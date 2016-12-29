package cito.sockjs;

import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cito.sockjs.ws.WebSocketServer;

/**
 * Initialises the SockJS runtime within the container. The base path will be {@code <context-root>/sockjs}. If this is
 * not suitable use the {@code web.xml} {@code context-param}:
 * 
 * <pre>
 *  <web-app>
 *    <context-param>
 *      <param-name>sockjs.path</param-name>
 *      <param-value>my-path</param-value>
 *    </context-param>
 *  </web-app>
 * </pre>
 * 
 * @author Daniel Siviter
 * @since v1.0 [28 Dec 2016]
 */
public class ContainerInitialiser implements ServletContainerInitializer {
	private static final String BASE_PATH = "%s/{server}/{session}/%s";
	private static final Logger LOG = LoggerFactory.getLogger(ContainerInitialiser.class);

	@Override
	public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
		final String path = getInitParam(ctx, "sockjs.path", "sockjs");
		LOG.info("Using path: {}", path);

		ctx.addServlet("sockjs-greeting",		GreetingServlet.class).addMapping(path);
		ctx.addServlet("sockjs-info",			InfoServlet.class).addMapping(String.format("%s/info", path));
		ctx.addServlet("sockjs-iframe",			IFrameServlet.class).addMapping(String.format("%s/iframe", path));
		ctx.addServlet("sockjs-eventsource",	EventSourceServlet.class).addMapping(String.format(BASE_PATH, path, "eventsource"));
//		ctx.addServlet("sockjs-xhr", EventSourceServlet.class).addMapping(String.format(BASE_PATH, path, "xhr"));
//		etc...

		final ServerEndpointConfig rawWebSocket = ServerEndpointConfig.Builder
				.create(WebSocketServer.class, String.format("%s/websocket", path))
				.build();
		final ServerEndpointConfig webSocket = ServerEndpointConfig.Builder
				.create(WebSocketServer.class, String.format(BASE_PATH, path, "websocket"))
				.build();

		final ServerContainer serverContainer = (ServerContainer) ctx.getAttribute(ServerContainer.class.getName());
		try {
			serverContainer.addEndpoint(rawWebSocket);
			serverContainer.addEndpoint(webSocket);
		} catch (DeploymentException e) {
			throw new ServletException(e);
		}
	}


	// --- Static Methods ---

	/**
	 * 
	 * @param ctx
	 * @param name
	 * @param _default
	 * @return
	 */
	public static String getInitParam(ServletContext ctx, String name, String _default) {
		final String value = ctx.getInitParameter(name);
		return value != null ? value : _default;
	}
}
