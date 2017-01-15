package cito.sockjs;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [3 Jan 2017]
 */
public enum Util { ;
	/**
	 * 
	 * @param r
	 * @return
	 */
	public static String session(HttpServletRequest r) {
		final String[] requestUriTokens = r.getRequestURI().split("/");
		if (requestUriTokens.length < 3) {
			throw new IllegalArgumentException("'session' not found! [" + r.getRequestURI() + "]");
		}
		return requestUriTokens[requestUriTokens.length - 2];
	}

	/**
	 * 
	 * @param r
	 * @return
	 */
	public static String server(HttpServletRequest r) {
		final String[] requestUriTokens = r.getRequestURI().split("/");
		if (requestUriTokens.length < 3) {
			throw new IllegalArgumentException("'server' not found! [" + r.getRequestURI() + "]");
		}
		return requestUriTokens[requestUriTokens.length - 3];
	}
	
	/**
	 * 
	 * @param r
	 * @return
	 */
	public static Map<String, String> pathParams(HttpServletRequest r) {
		final Map<String, String> pathParams = new HashMap<>();
		pathParams.put("session", session(r));
		pathParams.put("server", server(r));
		return Collections.unmodifiableMap(pathParams);
	}
}
