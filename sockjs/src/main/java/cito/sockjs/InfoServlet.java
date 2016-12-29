package cito.sockjs;

import static cito.sockjs.Headers.CONTENT_TYPE;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Dec 2016]
 */
@WebServlet(name = "SockJs Info Servlet", asyncSupported = true)
public class InfoServlet extends HttpServlet {
	private static final long serialVersionUID = 4503408709378376273L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.getWriter().print("Welcome to SockJS!\n");
	}
}
