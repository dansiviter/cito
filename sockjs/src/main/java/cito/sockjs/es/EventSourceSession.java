package cito.sockjs.es;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.websocket.RemoteEndpoint.Basic;

import cito.sockjs.AbstractBasic;
import cito.sockjs.AsyncHandler;
import cito.sockjs.ServletSession;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Jul 2016]
 */
public class EventSourceSession extends ServletSession {
	private static final String DELIMITER = "/n/n";

	public EventSourceSession(AsyncContext asyncCtx) {
		super(asyncCtx);
		this.asyncCtx.addListener(new AsyncHandler() {
			@Override
			public void onComplete(AsyncEvent event) throws IOException {
				
			}
		});
	}

	@Override
	protected Basic createBasic() {
		return new EventSourceBasic(this.asyncCtx);
	}

	@Override
	public void close() {
		this.asyncCtx.complete();
	}


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [29 Jul 2016]
	 */
	public class EventSourceBasic extends AbstractBasic {
		
		protected EventSourceBasic(AsyncContext asyncCtx) {
			super(asyncCtx);
		}

		@Override
		public void sendText(String text) throws IOException {
			asyncCtx.getResponse().getWriter().append(text).append(DELIMITER);
		}

		@Override
		public void sendText(String partialMessage, boolean isLast) throws IOException {
			final Writer writer = asyncCtx.getResponse().getWriter();
			writer.append(partialMessage);
			if (isLast)
				writer.append(DELIMITER);
		}
	}
}
