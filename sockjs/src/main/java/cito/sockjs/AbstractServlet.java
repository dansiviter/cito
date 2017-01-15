package cito.sockjs;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [4 Jan 2017]
 */
public abstract class AbstractServlet extends HttpServlet {
	private static final long serialVersionUID = 917110139775886906L;

	protected final Context ctx;

	protected AbstractServlet(Context ctx) {
		this.ctx = ctx;
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		// XXX do we need the wrappers?!
		req = new SockJsRequest(req);
		res = new SockJsResponse(res);
		final AsyncContext asyncContext = req.startAsync(req, res);
		asyncContext.setTimeout(0); // eternal
		super.service(req, res);
	}

	protected Session getSession(String sessionId) throws ServletException {
		Session session = this.ctx.getSession(sessionId);
		if (session == null) {
			this.ctx.register(session = createSession(sessionId, this.ctx), ctx.getInitialiser().createEndpoint());
		}
		return session;
	}

	/**
	 * 
	 * @param sessionId
	 * @param asyncCtx
	 * @return
	 */
	protected abstract Session createSession(String sessionId, AsyncContext asyncCtx);
}
