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

import javax.inject.Inject;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [9 Aug 2016]
 */
@WebListener("Monitors the state of the HTTP sessions on the server.")
public class HttpSessionHandler implements HttpSessionListener {
	@Inject
	private HttpSessionRegistry registry;
	
	@Override
	public void sessionCreated(HttpSessionEvent se) {
		this.registry.register(se.getSession());
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		this.registry.unregister(se.getSession());
	}
}
