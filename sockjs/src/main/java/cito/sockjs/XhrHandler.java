package cito.sockjs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.lang3.StringEscapeUtils;

import cito.sockjs.nio.WriteStream;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [3 Jan 2017]
 */
public class XhrHandler extends AbstractHandler {
	private static final long serialVersionUID = -527374807374550532L;
	private static final byte[] SEPARATOR = "\n".getBytes(StandardCharsets.UTF_8);
	private static final String CONTENT_TYPE_VALUE = "application/javascript; charset=UTF-8";

	/**
	 * 
	 * @param ctx
	 */
	public XhrHandler(Servlet servlet) {
		super(servlet);
	}

	@Override
	public void service(HttpAsyncContext asyncCtx) throws ServletException, IOException {
		final HttpServletRequest req = asyncCtx.getRequest();
		final HttpServletResponse res = asyncCtx.getResponse();

		if ("OPTIONS".equals(req.getMethod())) {
			options(asyncCtx, "OPTIONS", "POST");
			return;
		}
		if (!"POST".equals(req.getMethod())) {
			sendErrorNonBlock(asyncCtx, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}

		ServletSession session = this.servlet.getSession(req);
		res.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_VALUE);
		setCors(req, res);
		setCacheControl(asyncCtx);

		if (session == null) {
			this.servlet.log("New session! [" + Util.session(this.servlet, req) + "]");
			session = this.servlet.createSession(req);
			final ServletOutputStream out = res.getOutputStream();
			out.write(OPEN_FRAME);
			out.write(SEPARATOR);
			asyncCtx.complete();
			return;
		}

		final Pipe pipe = Pipe.open();
		final WritableByteChannel dest = pipe.sink();
		try (JsonGenerator generator = Json.createGenerator(Channels.newOutputStream(dest))) {
			dest.write(ByteBuffer.wrap(ARRAY_FRAME));
			generator.writeStartArray();
			try (Sender sender = new XhrSender(session, generator)) {
				session.setSender(sender);
			}
			generator.writeEnd();
			generator.flush();
			dest.write(ByteBuffer.wrap(SEPARATOR));
		}

		res.getOutputStream().setWriteListener(new WriteStream(asyncCtx, pipe.source()));
	}


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [18 Feb 2017]
	 */
	private class XhrSender implements Sender {
		private final ServletSession session;
		private final JsonGenerator generator;

		public XhrSender(ServletSession session, JsonGenerator generator) {
			this.session = session;
			this.generator = generator;
		}

		@Override
		public void send(String frame, boolean last) throws IOException {
			this.generator.write(StringEscapeUtils.escapeJson(frame));
		}

		@Override
		public void close() throws IOException {
			this.session.setSender(null);
		}
	}
}
