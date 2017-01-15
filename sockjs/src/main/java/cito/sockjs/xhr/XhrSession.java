package cito.sockjs.xhr;

import java.io.IOException;
import java.io.Writer;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;

import javax.servlet.AsyncContext;
import javax.websocket.RemoteEndpoint.Basic;

import cito.sockjs.AbstractBasic;
import cito.sockjs.ServletSession;
import cito.sockjs.es.EventSourceSession.EventSourceBasic;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [5 Jan 2017]
 */
public class XhrSession extends ServletSession {

	public XhrSession(AsyncContext asyncCtx) {
		super(asyncCtx);
	}

	@Override
	protected Basic createBasic() {
		return new XhrBasic(this.asyncCtx);
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
	public class XhrBasic extends AbstractBasic {
		private final Queue<String> queue = new LinkedTransferQueue<>();
		
		protected XhrBasic(AsyncContext asyncCtx) {
			super(asyncCtx);
		}

		@Override
		public void sendText(String text) throws IOException {
			queue.add(text)
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
