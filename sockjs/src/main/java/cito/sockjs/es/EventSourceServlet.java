package cito.sockjs.es;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;
import javax.ws.rs.core.MediaType;

import cito.sockjs.AbstractServlet;
import cito.sockjs.AsyncHandler;
import cito.sockjs.Context;
import cito.sockjs.Util;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Dec 2016]
 */
public class EventSourceServlet extends AbstractServlet {
	private static final long serialVersionUID = -6749385462053436601L;

	/**
	 * A {@code String} constant representing "{@value #TEXT_EVENTSTREAM}" media type.
	 */
	public final static String TEXT_EVENTSTREAM = "text/event-stream";
	/**
	 * A {@link MediaType} constant representing "{@value #TEXT_EVENTSTREAM}" media type.
	 */
	public final static MediaType TEXT_EVENTSTREAM_TYPE = new MediaType("text", "event-stream");

	public EventSourceServlet(Context ctx) {
		super(ctx);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException { 
		res.setContentType(TEXT_EVENTSTREAM);
		res.setCharacterEncoding(StandardCharsets.UTF_8.name());

		final Session session = getSession(Util.session(req));
		
	}

	@Override
	protected Session createSession(String sessionId, AsyncContext asyncCtx) {
		return new EventSourceSession(asyncCtx);
	}
}
