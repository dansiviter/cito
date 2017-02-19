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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [4 Jan 2017]
 */
public class Context {
	private final Map<String, ServletSession> sessions = new ConcurrentHashMap<>();

	private final Config config;

	private boolean webSocketSupported;

	public Context(Config config) {
		this.config = config;
	}

	public Config getConfig() {
		return config;
	}

	public ServletSession register(ServletSession session) {
		return this.sessions.putIfAbsent(session.getId(), session);
	}

	/**
	 * 
	 * @param sessionId
	 * @return
	 */
	public ServletSession getSession(String sessionId) {
		return this.sessions.get(sessionId);
	}

	/**
	 * 
	 * @param sessionId
	 */
	public void unregister(String sessionId) {
		this.sessions.remove(sessionId);
	}

	/**
	 * @return the webSocketSupported
	 */
	public boolean isWebSocketSupported() {
		return webSocketSupported;
	}

	/**
	 * 
	 * @param supported
	 */
	void setWebSocketSupported(boolean supported) {
		this.webSocketSupported = supported;
	}
}
