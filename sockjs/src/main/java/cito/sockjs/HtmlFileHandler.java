/*

 * Copyright 2016-2017 Daniel Siviter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cito.sockjs;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Queue;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cito.sockjs.nio.WriteStream;

/**
 * @author Daniel Siviter
 * @since v1.0 [1 Mar 2017]
 */
public class HtmlFileHandler extends AbstractSessionHandler {
	private static final long serialVersionUID = -4614348605938993415L;

	static final String HTMLFILE = "htmlfile";

	private String prelude;

	/**
	 * 
	 * @param servlet
	 */
	public HtmlFileHandler(Servlet servlet) {
		super(servlet, "text/html;charset=UTF-8", true, "GET");
	}

	@Override
	public HtmlFileHandler init() throws ServletException {
		try {
			this.prelude = Util.resourceToString(getClass(), "htmlfile.html");
			this.prelude = StringUtils.rightPad(this.prelude, 1_024);
			this.prelude += "\r\n";
		} catch (IOException e) {
			throw new ServletException("Unable to load template!", e);
		}
		return this;
	}

	@Override
	protected void handle(HttpAsyncContext async, ServletSession session, boolean initial)
			throws ServletException, IOException
	{
		final HttpServletResponse res = async.getResponse();

		final Pipe pipe = Pipe.open();
		session.setSender(new HtmlFileSender(session, pipe.sink()));

		final String callback = getCallback(async.getRequest());
		if (callback == null || callback.isEmpty()) {
			this.log.warn("Callback expected.");
			sendErrorNonBlock(async, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "\"callback\" parameter required");
			return;
		}

		res.getOutputStream().setWriteListener(new WriteStream(async, pipe.source()));
		final String prelude = String.format(this.prelude, callback);
		pipe.sink().write(UTF_8.encode(CharBuffer.wrap(prelude)));
		if (initial) {
			pipe.sink().write(UTF_8.encode(CharBuffer.wrap("<script>\np(\"o\");\n</script>\r\n")));
		}
	}


	// --- Static Methods ---

	/**
	 * 
	 * @param request
	 * @return
	 */
	private static String getCallback(HttpServletRequest request) {
		String value = request.getParameter("c");
		try {
			return StringUtils.isEmpty(value) ? null : URLDecoder.decode(value, "UTF8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e); // UTF-8 should always be supported!
		}
	}


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [25 Feb 2017]
	 */
	private class HtmlFileSender implements Sender {
		private final Logger log = LoggerFactory.getLogger(HtmlFileSender.class);
		private final ServletSession session;
		private final WritableByteChannel dest;
		private int bytesSent;

		public HtmlFileSender(ServletSession session, SinkChannel dest) {
			this.session = session;
			this.dest = dest;
		}

		@Override
		public void send(Queue<String> frames) throws IOException {
			while (!frames.isEmpty()) {
				String frame = frames.poll();
				this.log.debug("Flushing frame. [sessionId={},frame={}]", this.session.getId(), frame);
				frame = StringEscapeUtils.escapeJson(frame);
				// +34 represents the possible start/end frame
				final CharBuffer buf = CharBuffer.allocate(frame.length() + 34);
				buf.append("<script>\np(\"a[\\\"").append(frame).append("\\\"]\");\n</script>\r\n").flip();
				final ByteBuffer byteBuf = UTF_8.encode(buf);
				this.dest.write(byteBuf);
				this.bytesSent += byteBuf.limit();
				final boolean limitReached = this.bytesSent >= servlet.getConfig().maxStreamBytes();
				if (limitReached) {
					this.log.debug("Limit to streaming bytes reached. Closing sender.");
					close();
					return;
				}
			}
		}

		@Override
		public void close() throws IOException {
			this.session.setSender(null);
			this.dest.close();
		}
	}
}
