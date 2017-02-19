package cito.sockjs;

import static cito.sockjs.Headers.CONTENT_TYPE;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Dec 2016]
 */
public class GreetingHandler extends HttpServlet {
	private static final long serialVersionUID = -3437734574704352295L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (req.getPathInfo() != null) {
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		resp.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.getWriter().print("Welcome to SockJS!\n");
	}
}
