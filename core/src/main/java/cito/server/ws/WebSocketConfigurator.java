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
package cito.server.ws;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

import cito.server.SecurityContext;

/**
 * Links the WebSocket endpoint to the security mechanism.
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Sep 2016]
 */
public class WebSocketConfigurator extends Configurator {
	@Override
	public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
		final HttpSession httpSession = (HttpSession) request.getHttpSession();
		final SecurityContext securityCtx = new WebSocketSecurityContext(request);
		sec.getUserProperties().put(httpSession.getId(), securityCtx);
	}
}
