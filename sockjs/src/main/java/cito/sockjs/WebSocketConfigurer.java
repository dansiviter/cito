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

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.websocket.Extension;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 * @author Daniel Siviter
 * @since v1.0 [24 Feb 2017]
 */
public class WebSocketConfigurer extends ServerEndpointConfig.Configurator {
	private final ServerEndpointConfig.Configurator delegate;

	/**
	 * 
	 */
	public WebSocketConfigurer(ServerEndpointConfig.Configurator delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean checkOrigin(String originHeaderValue) {
		return this.delegate.checkOrigin(originHeaderValue);
	}

	@Override
	public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
		final HttpSession httpSession = (HttpSession) request.getHttpSession();
		sec.getUserProperties().put(ServletContext.class.getSimpleName(), httpSession.getServletContext());
		this.delegate.modifyHandshake(sec, request, response);
	}

	@Override
	public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
		return delegate.getEndpointInstance(endpointClass);
	}

	@Override
	public List<Extension> getNegotiatedExtensions(List<Extension> installed, List<Extension> requested) {
		return this.delegate.getNegotiatedExtensions(installed, requested);
	}

	@Override
	public String getNegotiatedSubprotocol(List<String> supported, List<String> requested) {
		return this.delegate.getNegotiatedSubprotocol(supported, requested);
	}
}
