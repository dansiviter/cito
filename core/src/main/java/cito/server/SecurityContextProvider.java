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

import javax.enterprise.inject.Produces;
import javax.websocket.Session;

import org.slf4j.LoggerFactory;

import cito.annotation.WebSocketScope;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [27 Oct 2016]
 */
public class SecurityContextProvider {
	/**
	 * 
	 * @param session
	 * @return
	 */
	@Produces @WebSocketScope
	public SecurityContext session(Session session) {
		LoggerFactory.getLogger(SecurityContext.class).info("Returning SecurityContext... [sessionId={}]", session.getId());
		return (SecurityContext) session.getUserProperties().get(SecurityContext.class.getSimpleName());
	}
}
