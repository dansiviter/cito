package cito.sockjs.xhr;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;

import cito.sockjs.AbstractServlet;
import cito.sockjs.Context;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [3 Jan 2017]
 */
public class XhrServlet extends AbstractServlet {
	private static final long serialVersionUID = -527374807374550532L;

	/**
	 * 
	 * @param ctx
	 */
	public XhrServlet(Context ctx) {
		super(ctx);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		System.out.println(req.getRequestURI() + " recieved in 'XhrServlet");
	}

	@Override
	protected Session createSession(String sessionId, AsyncContext asyncCtx) {
		return new XhrSession(asyncCtx);
	}
}
