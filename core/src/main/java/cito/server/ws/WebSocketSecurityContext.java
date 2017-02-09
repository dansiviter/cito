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

import java.security.Principal;

import javax.websocket.server.HandshakeRequest;

import cito.server.SecurityContext;

/**
 * Defines {@link javax.ws.rs.core.SecurityContext} for WebSocket connections.
 * 
 * @author Daniel Siviter
 * @since v1.0 [17 Aug 2016]
 */
public class WebSocketSecurityContext implements SecurityContext {
	private static final long serialVersionUID = 2495929824931036726L;

	private final HandshakeRequest req;

	public WebSocketSecurityContext(HandshakeRequest req) {
		this.req = req;
	}

	@Override
	public Principal getUserPrincipal() {
		return this.req.getUserPrincipal();
	}

	@Override
	public boolean isUserInRole(String role) {
		return this.req.isUserInRole(role);
	}
}
