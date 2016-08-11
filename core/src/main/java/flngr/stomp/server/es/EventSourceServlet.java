package flngr.stomp.server.es;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import flngr.servlet.AsyncHandler;
import flngr.stomp.server.SessionRegistry;
import flngr.stomp.server.xhr.AbstractXhrServlet;

//@WebServlet(urlPatterns={"/*/*/eventsource"}, asyncSupported=true)
public class EventSourceServlet extends AbstractXhrServlet {
	/**
	 * A {@code String} constant representing "{@value #TEXT_EVENTSTREAM}" media type.
	 */
	public final static String TEXT_EVENTSTREAM = "text/event-stream";
	/**
	 * A {@link MediaType} constant representing "{@value #TEXT_EVENTSTREAM}" media type.
	 */
	public final static MediaType TEXT_EVENTSTREAM_TYPE = new MediaType("text", "event-stream");

//	@Inject
	private SessionRegistry registry;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException { 
		res.setContentType(TEXT_EVENTSTREAM);
		res.setCharacterEncoding(StandardCharsets.UTF_8.name());

		// TODO, use /<session>/<server> as a key for #getId?

		final AsyncContext asyncCtx = req.startAsync();
		final EventSourceSession session = new EventSourceSession(req, Collections.emptyMap(), asyncCtx);
		asyncCtx.addListener(new AsyncHandler() {
			@Override
			public void onComplete(AsyncEvent event) throws IOException {
				registry.unregister(session);
			}
		});
		this.registry.register(session);
	}
}
