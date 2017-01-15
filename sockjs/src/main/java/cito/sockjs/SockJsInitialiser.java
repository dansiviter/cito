package cito.sockjs;

import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.HandlesTypes;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cito.sockjs.xhr.XhrServlet;

/**
 * Initialises the SockJS runtime within the container via the {@link Initialiser} class.
 * 
 * @author Daniel Siviter
 * @since v1.0 [28 Dec 2016]
 * @see Initialiser
 */
@HandlesTypes(Initialiser.class)
public class SockJsInitialiser implements ServletContainerInitializer {
	private static final String BASE_PATH = "%s/{server}/{session}/%s";
	private static final Logger LOG = LoggerFactory.getLogger(SockJsInitialiser.class);

	@Override
	public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
		final List<Initialiser> initializers = new LinkedList<Initialiser>();

		if (c != null) {
			for (Class<?> cls : c) {
				if (!cls.isInterface() && !Modifier.isAbstract(cls.getModifiers()) &&
						Initialiser.class.isAssignableFrom(cls)) {
					try {
						initializers.add((Initialiser) cls.newInstance());
					}
					catch (Throwable e) {
						throw new ServletException("Failed to instantiate SockJsInitialiser class!", e);
					}
				}
			}
		}

		if (initializers.isEmpty()) {
			ctx.log("No SockJsInitialiser types detected on classpath.");
			return;
		}

		for (Initialiser initializer : initializers) {
			initialise(initializer, ctx);
		}
	}


	// --- Static Methods

	/**
	 * 
	 * @param initialiser
	 * @param ctx
	 * @throws ServletException
	 */
	private static void initialise(Initialiser initialiser, ServletContext ctx) throws ServletException {
		final String path = initialiser.path();
		LOG.info("Using path: {}", path);

		final Context sockJsCtx = new Context(initialiser);
		addServlet(ctx, "sockjs-greeting",		new GreetingServlet(sockJsCtx),	path);
		addServlet(ctx, "sockjs-info",			new InfoServlet(sockJsCtx),		String.format("%s/info", path));
		addServlet(ctx, "sockjs-iframe",		new IFrameServlet(sockJsCtx),	String.format("%s/iframe", path));
		addServlet(ctx, "sockjs-xhr",			new XhrServlet(sockJsCtx), 
				String.format(BASE_PATH, path, "xhr"),
				String.format(BASE_PATH, path, "xhr_send"));
//		ctx.addServlet("sockjs-eventsource",	EventSourceServlet.class).addMapping(String.format(BASE_PATH, path, "eventsource"));
//		etc...

		final ServerContainer serverContainer = (ServerContainer) ctx.getAttribute(ServerContainer.class.getName());
		try {
			serverContainer.addEndpoint(createConfig(initialiser, String.format("%s/websocket", path)).build());
			serverContainer.addEndpoint(createConfig(initialiser, String.format(BASE_PATH, path, "websocket")).build());
		} catch (DeploymentException e) {
			throw new ServletException("Unable to deploy WebSocket!", e);
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

	/**
	 * 
	 * @param ctx
	 * @param name
	 * @param servlet
	 * @param paths
	 */
	private static void addServlet(ServletContext ctx, String name, Servlet servlet, String... paths) {
		final ServletRegistration.Dynamic reg = ctx.addServlet(name, servlet);
		reg.addMapping(paths);
		reg.setAsyncSupported(true);
	}

	/**
	 * 
	 * @param cls
	 * @param path
	 * @return
	 */
	public static ServerEndpointConfig.Builder createConfig(Initialiser customiser, String path) {
		return ServerEndpointConfig.Builder
				.create(customiser.endpointClass(), path)
				.decoders(customiser.decoders())
				.encoders(customiser.encoders())
				.configurator(customiser.serverEndpointConfigurator())
				.extensions(customiser.extensions())
				.subprotocols(customiser.subprotocols());
	}
}
