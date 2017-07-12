/*
 * Copyright 2016-2017 Daniel Siviter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cito.sockjs;

import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

//import javax.servlet.Servlet;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.HandlesTypes;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initialises the SockJS runtime within the container via the {@link Config} class.
 * 
 * @author Daniel Siviter
 * @since v1.0 [28 Dec 2016]
 * @see Config
 */
@HandlesTypes(Config.class)
public class Initialiser implements ServletContainerInitializer {
	private static final Logger LOG = LoggerFactory.getLogger(Initialiser.class);

	@Override
	public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
		LOG.info("Initialising SockJS. [{}]", c);
		final List<Config> initializers = new LinkedList<Config>();

		if (c != null) {
			for (Class<?> cls : c) {
				if (!cls.isInterface() && !Modifier.isAbstract(cls.getModifiers()) &&
						Config.class.isAssignableFrom(cls)) {
					try {
						initializers.add((Config) cls.newInstance());
					} catch (Exception e) {
						throw new ServletException("Failed to instantiate SockJsInitialiser class!", e);
					}
				}
			}
		}

		if (initializers.isEmpty()) {
			ctx.log("No SockJsInitialiser types detected on classpath.");
			return;
		}

		for (Config initializer : initializers) {
			initialise(initializer, ctx);
		}
	}


	// --- Static Methods

	/**
	 * 
	 * @param config
	 * @param servletCtx
	 * @throws ServletException
	 */
	private static void initialise(Config config, ServletContext servletCtx) throws ServletException {
		final String name = config.name();
		final String path = config.path();
		LOG.info("Initialising SockJS. [name={},path={},servletContext={}]", name, path, servletCtx);

		final Servlet servlet = new Servlet(config);
		addServlet(servletCtx, "sockjs-" + name, servlet, String.format("/%s/*", path));

		final ServerContainer serverContainer = (ServerContainer) servletCtx.getAttribute(ServerContainer.class.getName());
		try {
			serverContainer.addEndpoint(createConfig(
					servlet, config, config.endpointClass(),		String.format("/%s/websocket", path)).build());
			serverContainer.addEndpoint(createConfig(
					servlet, config, WebSocketEndpoint.class, 	String.format("/%s/{server}/{session}/websocket", path)).build());
			servlet.setWebSocketSupported(true);
		} catch (DeploymentException e) {
			LOG.warn("Unable to deploy WebSockets!", e);
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
	public static ServerEndpointConfig.Builder createConfig(Servlet servlet, Config customiser, Class<? extends Endpoint> endpointClass, String path) {
		return ServerEndpointConfig.Builder
				.create(endpointClass, path)
				.decoders(customiser.decoders())
				.encoders(customiser.encoders())
				.configurator(new WebSocketConfigurer(servlet, customiser.serverEndpointConfigurator()))
				.extensions(customiser.extensions())
				.subprotocols(customiser.subprotocols());
	}
}
