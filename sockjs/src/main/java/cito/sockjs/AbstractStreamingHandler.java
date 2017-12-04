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
import java.nio.channels.Pipe;
import java.util.Queue;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cito.sockjs.nio.WriteStream;

/**
 * @author Daniel Siviter
 * @since v1.0 [3 Apr 2017]
 */
public abstract class AbstractStreamingHandler extends AbstractSessionHandler {
	protected static final FrameFormat DEFAULT_FORMAT = c -> new StringBuilder(c).append('\n');

	/**
	 * 
	 * @param servlet
	 * @param mediaType
	 * @param method
	 * @param prelude
	 */
	public AbstractStreamingHandler(Servlet servlet, String mediaType, String method) {
		super(servlet, mediaType, true, method);
	}

	/**
	 * 
	 * @param async
	 * @param session the session.
	 * @param initial if {@code true} then this is the first request for a session.
	 * @param format the frame format.
	 * @param prelude the new request prelude.
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void handle(
			HttpAsyncContext async, ServletSession session, boolean initial, FrameFormat format, Prelude prelude)
	throws ServletException, IOException
	{
		this.log.info("New request. [sessionId={},initial={}]", session.getId(), initial); // FIXME remove this when Travis CI issues are addressed
		final HttpServletResponse res = async.getResponse();

		final Pipe pipe = Pipe.open();
		session.setSender(new StreamingSender(session, format, pipe));
		res.getOutputStream().setWriteListener(new WriteStream(async, pipe.source()));

		write(pipe, prelude.get());
		if (initial) {
			write(pipe, format.format("o"));
		} else if (!session.isOpen()) {
			this.log.info("Session closed! [{}]", session.getId());
			write(pipe, closeFrame(3000, "Go away!"), "\n");
			pipe.sink().close();
		} 
	}


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [25 Feb 2017]
	 */
	private class StreamingSender implements Sender {
		private final Logger log = LoggerFactory.getLogger(StreamingSender.class);
		private final ServletSession session;
		private final FrameFormat format;
		private final Pipe pipe;
		private int bytesSent;

		public StreamingSender(ServletSession session, FrameFormat format, Pipe pipe) throws IOException {
			this.session = session;
			this.format = format;
			this.pipe = pipe;
		}

		@Override
		public void send(Queue<String> frames) throws IOException {
			while (!frames.isEmpty()) {
				String frame = frames.poll();
				this.log.info("Flushing frame. [sessionId={},frame={}]", this.session.getId(), frame);
				frame = StringEscapeUtils.escapeJson(frame);
				final StringBuilder buf = new StringBuilder("a[\"").append(frame).append("\"]");
				this.bytesSent += write(pipe, format.format(buf));
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
			this.log.info("Closing sender. [sessionId={},sender={}]", this.session.getId());
			this.session.setSender(null);
			this.pipe.sink().close();
		}
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [7 Apr 2017]
	 */
	@FunctionalInterface
	protected interface FrameFormat {
		CharSequence format(CharSequence frame);
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [7 Apr 2017]
	 */
	@FunctionalInterface
	protected interface Prelude {
		CharSequence get();
	}
}
