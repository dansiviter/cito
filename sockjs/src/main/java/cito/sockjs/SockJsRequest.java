package cito.sockjs;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [5 Jan 2017]
 */
public class SockJsRequest extends HttpServletRequestWrapper {

	public SockJsRequest(HttpServletRequest request) {
		super(request);
	}

}
