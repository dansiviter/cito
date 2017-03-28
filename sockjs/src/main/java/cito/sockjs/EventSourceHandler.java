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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cito.sockjs.nio.WriteStream;

/**
 * Handles EventSource ({@code <server>/session/eventsource}) connections.
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Feb 2017]
 */
public class EventSourceHandler extends AbstractSessionHandler {
	private static final long serialVersionUID = -527374807374550532L;

	static final String EVENTSOURCE = "eventsource";
	private static final String CONTENT_TYPE_VALUE = "text/event-stream;charset=UTF-8";
	private static final String PRELUDE = "\r\n";

	/**
	 * 
	 * @param ctx
	 */
	public EventSourceHandler(Servlet servlet) {
		super(servlet, CONTENT_TYPE_VALUE, true, "GET");
	}

	@Override
	protected void handle(HttpAsyncContext async, ServletSession session, boolean initial)
			throws ServletException, IOException
	{
		final HttpServletResponse res = async.getResponse();

		final Pipe pipe = Pipe.open();
		session.setSender(new EventSourceSender(session, pipe.sink()));
		res.getOutputStream().setWriteListener(new WriteStream(async, pipe.source()));

		pipe.sink().write(UTF_8.encode(CharBuffer.wrap(PRELUDE)));
		if (initial) {
			pipe.sink().write(UTF_8.encode(CharBuffer.wrap("data: o\r\n\r\n")));
		} else if (!session.isOpen()) {
			this.log.info("Session closed! [{}]", session.getId());
			pipe.sink().write(UTF_8.encode(closeFrame(3000, "Go away!", "\n")));
			pipe.sink().close();
		} 
	}


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [25 Feb 2017]
	 */
	private class EventSourceSender implements Sender {
		private final Logger log = LoggerFactory.getLogger(EventSourceSender.class);
		private final ServletSession session;
		private final WritableByteChannel dest;
		private int bytesSent;

		public EventSourceSender(ServletSession session, SinkChannel dest) throws IOException {
			this.session = session;
			this.dest = dest;
		}

		@Override
		public void send(Queue<String> frames) throws IOException {
			while (!frames.isEmpty()) {
				String frame = frames.poll();
				this.log.debug("Flushing frame. [sessionId={},frame={}]", this.session.getId(), frame);
				frame = StringEscapeUtils.escapeJson(frame);
				// +15 represents the possible start/end frame
				final CharBuffer buf = CharBuffer.allocate(frame.length() + 15);
				buf.append("data: a[\"").append(frame).append("\"]\r\n\r\n").flip();
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
