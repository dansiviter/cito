package civvi.stomp.server.es;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.websocket.EncodeException;
import javax.websocket.RemoteEndpoint.Basic;

import civvi.stomp.server.AbstractSession;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Jul 2016]
 */
public class EventSourceSession extends AbstractSession {
	private static final String DELIMITER = "/n/n";

	private final AsyncContext asyncCtx;

	public EventSourceSession(AsyncContext asyncCtx) {
		this.asyncCtx = asyncCtx;
	}

	@Override
	public String getId() {
		return ((HttpServletRequest) this.asyncCtx.getRequest()).getSession(true).getId();
	}

	@Override
	public Basic getBasicRemote() {
		return new BasicImpl();
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
	public class BasicImpl implements Basic {
		private boolean batchingAllowed;


		@Override
		public void setBatchingAllowed(boolean allowed) throws IOException {
			batchingAllowed = allowed;
		}

		@Override
		public boolean getBatchingAllowed() {
			return this.batchingAllowed;
		}

		@Override
		public void flushBatch() throws IOException {
			asyncCtx.getResponse().flushBuffer();
		}

		@Override
		public void sendPing(ByteBuffer applicationData) throws IOException, IllegalArgumentException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void sendPong(ByteBuffer applicationData) throws IOException, IllegalArgumentException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void sendText(String text) throws IOException {
			asyncCtx.getResponse().getWriter().append(text).append(DELIMITER);
		}

		@Override
		public void sendBinary(ByteBuffer data) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void sendText(String partialMessage, boolean isLast) throws IOException {
			final Writer writer = asyncCtx.getResponse().getWriter();
			writer.append(partialMessage);
			if (isLast)
				writer.append(DELIMITER);
		}

		@Override
		public void sendBinary(ByteBuffer partialByte, boolean isLast) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public OutputStream getSendStream() throws IOException {
			return asyncCtx.getResponse().getOutputStream();
		}

		@Override
		public Writer getSendWriter() throws IOException {
			return asyncCtx.getResponse().getWriter();
		}

		@Override
		public void sendObject(Object data) throws IOException, EncodeException {
			throw new UnsupportedOperationException();
		}
	}
}
