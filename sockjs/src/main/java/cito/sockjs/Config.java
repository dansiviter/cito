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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletException;
import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Extension;
import javax.websocket.server.ServerEndpointConfig;

/**
 * The entrypoint into initialising a SockJS runtime. Simply create an concrete version of this class and place it
 * within the classpath:
 * 
 * <pre>
 * public static class TestConfig implements Config {
 *   &#064;Override
 *   public String path() {
 *     return "my-endpoint";
 *   }
 * 
 *   &#064;Override
 *   public Class<? extends Endpoint> endpointClass() {
 *     return MyEndpoint.class;
 *   }
 * }
 * 
 * <pre>
 * The {@link Initialiser} class will be given this by the Servlet container using the
 * {@link ServletContainerInitializer} mechanism.
 * 
 * @author Daniel Siviter
 * @since v1.0 [4 Jan 2017]
 * @see Initialiser
 */
public interface Config extends EndpointConfig {
	/**
	 * @return the name of this SockJS deployment. This must be unique if deploying multiple instances within the same
	 * servlet container.
	 */
	default String name() {
		return getClass().getSimpleName();
	}

	
	/** 
	 * @return the class to use for the service. This must have a no-args contructor.
	 */
	Class<? extends Endpoint> endpointClass();

	/**
	 * @return an instance of the endpoint.
	 * @throws ServletException
	 * @see #endpointClass()
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
	 * @return the path of the service above the context root.
	 */
	String path();

	/**
	 * @return the permitted subprotocols. Not used!
	 */
	default List<String> subprotocols() {
		return Collections.emptyList();
	}

	/**
	 * @return the WebSocket extensions. Not used!
	 */
	default List<Extension> extensions() {
		return Collections.emptyList();
	}

	/**
	 * @return the encoder implementation classes, an empty list if none.
	 */
	default List<Class<? extends Encoder>> encoders() {
		return Collections.emptyList();
	}

	@Override
	default List<Class<? extends Encoder>> getEncoders() {
		return encoders();
	}

	/**
	 * @return the decoder implementation classes, an empty list if none.
	 */
	default List<Class<? extends Decoder>> decoders() {
		return Collections.emptyList();
	}

	@Override
	default List<Class<? extends Decoder>> getDecoders() {
		return decoders();
	}

	/**
	 * @return the maximum number of bytes before a stream connection (other that WebSocket) has to be recycled. Default
	 * is 128KB.
	 */
	default int maxStreamBytes() {
		return 131_072;
	}

	/**
	 * @return
	 */
	default Class<? extends ServerEndpointConfig.Configurator> endpointConfiguratorClass() {
		return null;
	}

	/**
	 * @return the SockJS script for injecting into the iFrame. Override if serving from your own CDN.
	 */
	default String sockJsUri() {
		return "//cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.1.2/sockjs.min.js";
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

	@Override
	default Map<String, Object> getUserProperties() {
		return Collections.emptyMap();
	}
}
