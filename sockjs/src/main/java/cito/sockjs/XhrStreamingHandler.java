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
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Queue;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import cito.sockjs.nio.WriteStream;

/**
 * Handles XHR Streaming ({@code /<server>/session/xhr_streaming}) connections.
 * 
 * @author Daniel Siviter
 * @since v1.0 [3 Jan 2017]
 */
public class XhrStreamingHandler extends AbstractSessionHandler {
	private static final long serialVersionUID = -527374807374550532L;

	static final String XHR_STREAMING = "xhr_streaming";
	private static final String CONTENT_TYPE_VALUE = "application/javascript;charset=UTF-8";
	private static final String PRELUDE = StringUtils.leftPad("\n", 2049, "h");

	/**
	 * 
	 * @param ctx
	 */
	public XhrStreamingHandler(Servlet servlet) {
		super(servlet, CONTENT_TYPE_VALUE, true, "POST");
	}

	@Override
	protected void handle(HttpAsyncContext async, ServletSession session, boolean initial)
	throws ServletException, IOException
	{
		final HttpServletResponse res = async.getResponse();

		final Pipe pipe = Pipe.open();
		session.setSender(new XhrStreamingSender(session, initial, pipe.sink()));
		res.getOutputStream().setWriteListener(new WriteStream(async, pipe.source()));
	}


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [18 Feb 2017]
	 */
	private class XhrStreamingSender implements Sender {
		private final ServletSession session;
		private final WritableByteChannel dest;
		private int bytesSent;

		public XhrStreamingSender(ServletSession session, boolean initial, SinkChannel dest) throws IOException {
			this.session = session;
			this.dest = dest;

			this.dest.write(UTF_8.encode(CharBuffer.wrap(PRELUDE)));

			if (initial) {
				this.dest.write(UTF_8.encode(CharBuffer.wrap("o\n")));
			}
		}

		@Override
		public void send(Queue<String> frames) throws IOException {
			while (!frames.isEmpty()) {
				String frame = frames.poll();
				servlet.log("Flushing frame. [sessionId=" + this.session.getId() + ",frame=" + frame + "]");
				frame = StringEscapeUtils.escapeJson(frame);
				final CharBuffer buf = CharBuffer.allocate(frame.length() + 6);
				buf.append("a[\"").append(frame).append("\"]\n").flip();
				final ByteBuffer byteBuf = UTF_8.encode(buf);
				this.dest.write(byteBuf);
				this.bytesSent += byteBuf.limit();
				final boolean limitReached = this.bytesSent >= servlet.getConfig().maxStreamBytes();
				if (limitReached) {
					servlet.log("Limit to streaming bytes reached. Closing sender.");
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
