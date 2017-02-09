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
package cito.server;

import java.io.Serializable;
import java.security.Principal;

import cito.annotation.WebSocketScope;

/**
 * Defines a way of accessing security related information via an injectable context. This can be accessed in the
 * {@link WebSocketScope}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [17 Aug 2016]
 */
public interface SecurityContext extends Serializable {
	/**
	 * Returns a {@link Principal} object containing the name of the current authenticated user. If the user
	 * has not been authenticated, the method returns null.
	 *
	 * @return a {@code Principal} containing the name of the user making this request; {@code null} if the user has
	 * not been authenticated
	 * @throws java.lang.IllegalStateException if called outside the scope of a websocket session.
	 */
	Principal getUserPrincipal();

	/**
	 * Returns a boolean indicating whether the authenticated user is included in the specified logical "role". If the
	 * user has not been authenticated, the method returns {@code false}.
	 *
	 * @param role a {@code String} specifying the name of the role
	 * @return a {@code boolean} indicating whether the user making the request belongs to a given role; {@code false}
	 * if the user has not been authenticated
	 * @throws java.lang.IllegalStateException if called outside the scope of a websocket session.
	 */
	boolean isUserInRole(String role);
}
