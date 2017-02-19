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

import javax.servlet.ServletException;
import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Extension;
import javax.websocket.server.ServerEndpointConfig;

/**
 * The entrypoint into initialising a SockJS runtime. Simply create an concrete version of this class and place it
 * within the classpath. The {@link Initialiser} class will be given this by the Servlet container.
 * 
 * @author Daniel Siviter
 * @since v1.0 [4 Jan 2017]
 * @see Initialiser
 */
public interface Config extends EndpointConfig {
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

	@Override
	default List<Class<? extends Encoder>> getEncoders() {
		return encoders();
	}

	/**
	 * 
	 * @return
	 */
	default List<Class<? extends Decoder>> decoders() {
		return Collections.emptyList();
	}

	@Override
	default List<Class<? extends Decoder>> getDecoders() {
		return decoders();
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
