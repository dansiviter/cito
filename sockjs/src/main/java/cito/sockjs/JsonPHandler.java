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
 * Handles JSONP Polling ({@code /<server>/<session>/jsonp}) connections.
 * 
 * @author Daniel Siviter
 * @since v1.0 [1 Mar 2017]
 */
public class JsonPHandler extends AbstractSessionHandler {
	static final String JSONP = "jsonp";

	/**
	 * 
	 * @param servlet
	 */
	public JsonPHandler(Servlet servlet) {
		super(servlet, "application/javascript;charset=UTF-8", true, "GET");
	}

	@Override
	protected void handle(HttpAsyncContext async, ServletSession session, boolean initial)
	throws ServletException, IOException
	{
		final HttpServletResponse res = async.getResponse();

		final Pipe pipe = Pipe.open();

		final String callback = getCallback(async.getRequest());
		if (callback == null || callback.isEmpty()) {
			this.log.warn("Callback expected.");
			sendNonBlock(async, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "\"callback\" parameter required");
			return;
		}

		res.getOutputStream().setWriteListener(new WriteStream(async, pipe.source()));

		if (initial) {
			write(pipe, callback, "(\"o\");\r\n");
			pipe.sink().close();
		} else if (!session.isOpen()) {
			this.log.info("Session closed! [{}]", session.getId());
			write(pipe, callback, "(\"", StringEscapeUtils.escapeEcmaScript(closeFrame(3000, "Go away!")), "\");\r\n");
			pipe.sink().close();
		} else {
			try (Sender sender = new JsonPSender(session, pipe.sink(), callback)) {
				if (!session.setSender(sender)) {
					this.log.warn("Connection still open! [{}]", session.getId());
					write(pipe, closeFrame(2010, "Another connection still open"), "\n");
				}
			}
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
	private class JsonPSender implements Sender {
		private final Logger log = LoggerFactory.getLogger(JsonPSender.class);
		private final ServletSession session;
		private final WritableByteChannel dest;
		private final String callback;

		public JsonPSender(ServletSession session, SinkChannel dest, String callback) {
			this.session = session;
			this.dest = dest;
			this.callback = callback;
		}

		@Override
		public void send(Queue<String> frames) throws IOException {
			this.dest.write(UTF_8.encode(callback));
			this.dest.write(UTF_8.encode("(\"a[\\\""));
			while (!frames.isEmpty()) {
				final String frame = frames.poll();
				this.log.debug("Flushing frame. [sessionId={},frame={}]", this.session.getId(), frame);
				this.dest.write(UTF_8.encode(StringEscapeUtils.escapeJson(frame)));
				if (!frames.isEmpty()) {
					this.dest.write(UTF_8.encode("\\\",\\\""));
				}
			}
			this.dest.write(UTF_8.encode("\\\"]\");\r\n"));
		}

		@Override
		public void close() throws IOException {
			this.session.setSender(null);
			this.dest.close();
		}
	}
}
