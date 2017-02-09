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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import cito.servlet.HttpFilter;

/**
 * Permits
 * 
 * @author Daniel Siviter
 * @since v1.0 [9 Aug 2016]
 */
@WebFilter(
		value = "/*",
		asyncSupported = true,
		description = "Permits the WebSocket implementation to get a handle on the HTTP sessionId")
public class SessionFilter extends HttpFilter {
	@Override
	public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
	throws IOException, ServletException
	{
		chain.doFilter(new SessionRequestWrapper(request), response);
	}


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [9 Aug 2016]
	 */
	private static class SessionRequestWrapper extends HttpServletRequestWrapper {
		private final Map<String, String[]> parameterMap;

		SessionRequestWrapper(HttpServletRequest request) {
			super(request);
			final Map<String, String[]> parameterMap = new HashMap<>(request.getParameterMap());
			parameterMap.put("httpSessionId", new String[] { request.getSession(true).getId() });
			this.parameterMap = Collections.unmodifiableMap(parameterMap);
		}

		@Override
		public Map<String, String[]> getParameterMap() {
			return this.parameterMap;
		}
	}
}
