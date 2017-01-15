package cito.sockjs;

import static cito.sockjs.Headers.CONTENT_TYPE;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Dec 2016]
 */
public class GreetingServlet extends AbstractServlet {
	private static final long serialVersionUID = -3437734574704352295L;

	public GreetingServlet(Context ctx) {
		super(ctx);
	}

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
