package cito.sockjs;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [3 Jan 2017]
 */
public enum Util { ;
	/**
	 * 
	 * @param config
	 * @param req
	 * @return
	 */
	private static String[] uriTokens(Config config, HttpServletRequest req) {
		final String[] tokens = req.getRequestURI().substring(config.path().length() + 2).split("/");
		if (tokens.length != 3) {
			throw new IllegalStateException("Invalid path! [" + req.getRequestURI() + "]");
		}
		return tokens;
	}

	/**
	 * 
	 * @param config
	 * @param req
	 * @return
	 */
	public static String session(Servlet servlet, HttpServletRequest req) {
		return uriTokens(servlet.ctx.getConfig(), req)[1];
	}

	/**
	 * 
	 * @param config
	 * @param req
	 * @return
	 */
	public static String session(Config config, HttpServletRequest req) {
		return uriTokens(config, req)[1];
	}

	/**
	 * 
	 * @param r
	 * @return
	 */
	public static String server(Config config, HttpServletRequest req) {
		return uriTokens(config, req)[0];
	}

	/**
	 * 
	 * @param r
	 * @return
	 */
	public static String type(Config config, HttpServletRequest req) {
		return uriTokens(config, req)[2];
	}

	/**
	 * 
	 * @param r
	 * @return
	 */
	public static Map<String, String> pathParams(Config config, HttpServletRequest req) {
		final Map<String, String> pathParams = new HashMap<>();
		pathParams.put("session", session(config, req));
		pathParams.put("server", server(config, req));
		return Collections.unmodifiableMap(pathParams);
	}

	/**
	 * 
	 * @param cs
	 * @return
	 */
	public static boolean isEmptyOrNull(CharSequence cs) {
		return isEmptyOrNull(cs, false);
	}

	/**
	 * 
	 * @param cs
	 * @param trim
	 * @return
	 */
	public static boolean isEmptyOrNull(CharSequence cs, boolean trim) {
		return cs == null || (trim ? cs.toString().trim() : cs).length() == 0;
	}

	/**
	 * 
	 * @param async
	 * @return
	 */
	public static ServletContext servletContext(HttpAsyncContext async) {
		return async.getRequest().getServletContext();
	}
}
