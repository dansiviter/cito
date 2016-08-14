package cito.stomp.server;

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
