package cito.sockjs;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [5 Jan 2017]
 */
public class SockJsResponse extends HttpServletResponseWrapper {

	public SockJsResponse(HttpServletResponse response) {
		super(response);
	}

}
