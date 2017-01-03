package cito.sockjs;

import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [3 Jan 2017]
 */
public enum Util { ;
	public static String session(HttpServletRequest r) {
		final String requestUri = r.getRequestURI();
		final int index = requestUri.lastIndexOf('/');
		return requestUri.substring(requestUri.lastIndexOf('/', index - 1) + 1, index);
	}
}
