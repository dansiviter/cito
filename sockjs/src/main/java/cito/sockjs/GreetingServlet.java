package cito.sockjs;

import static cito.sockjs.Headers.CONTENT_TYPE;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Dec 2016]
 */
@WebServlet(name = "SockJs Greeting Servlet", asyncSupported = true)
public class GreetingServlet extends HttpServlet {
	private static final long serialVersionUID = -3437734574704352295L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		System.out.println(req.getPathInfo());
		if (req.getPathInfo().length() > 1) {
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		resp.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.getWriter().print("Welcome to SockJS!\n");
	}
}
