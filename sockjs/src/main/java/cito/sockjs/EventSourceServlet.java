package cito.sockjs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Dec 2016]
 */
public class EventSourceServlet extends HttpServlet {
	private static final long serialVersionUID = -6749385462053436601L;

	private static final String TEXT_EVENTSTREAM = "text/event-stream";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType(TEXT_EVENTSTREAM);	
		resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

		final PrintWriter writer = resp.getWriter();

		for (int i = 0; i < 10; i++) {
			writer.write("data: "+ System.currentTimeMillis() +"\n\n");

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		writer.close();
	}
}
