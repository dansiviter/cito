package cito.sockjs;

import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.Endpoint;
import javax.websocket.Extension;
import javax.websocket.server.ServerEndpointConfig;

/**
 * The entrypoint into initialising a SockJS runtime. Simply create an concrete version of this class and place it
 * within the classpath. The {@link SockJsInitialiser} class will be given this by the Servlet container.
 * 
 * @author Daniel Siviter
 * @since v1.0 [4 Jan 2017]
 * @see SockJsInitialiser
 */
public interface Initialiser {
	/**
	 * 
	 * @return
	 */
	Class<? > endpointClass();

	/**
	 * 	
	 * @return
	 * @throws ServletException
	 */
	@SuppressWarnings("unchecked")
	default <E extends Endpoint> E createEndpoint() throws ServletException {
		try {
			return (E) endpointClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new ServletException(e);
		}
	}

	/**
	 * 
	 * @return
	 */
	String path();

	/**
	 * 
	 * @return
	 */
	default List<String> subprotocols() {
		return Collections.emptyList();
	}

	/**
	 * 
	 * @return
	 */
	default List<Extension> extensions() {
		return Collections.emptyList();
	}

	/**
	 * 
	 * @return
	 */
	default List<Class<? extends Encoder>> encoders() {
		return Collections.emptyList();
	}

	/**
	 * 
	 * @return
	 */
	default List<Class<? extends Decoder>> decoders() {
		return Collections.emptyList();
	}

	/**
	 * @return
	 */
	default Class<? extends ServerEndpointConfig.Configurator> endpointConfiguratorClass() {
		return null;
	}

	/**
	 * 
	 * @return
	 * @see #endpointConfiguratorClass()
	 */
	default ServerEndpointConfig.Configurator serverEndpointConfigurator() {
		final Class<? extends ServerEndpointConfig.Configurator> cls = endpointConfiguratorClass();
		if (cls == null) {
			return null;
		}

		try {
			return cls.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalArgumentException("Unable to intitialise '" + endpointConfiguratorClass() + "'!", e);
		}
	}
}
