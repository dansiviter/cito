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
import java.nio.CharBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.WritableByteChannel;

import javax.servlet.ServletException;

import org.apache.commons.lang3.StringEscapeUtils;

import cito.sockjs.nio.WriteStream;

/**
 * Handles XHR Polling ({@code <server>/session/xhr}) connections.
 * 
 * @author Daniel Siviter
 * @since v1.0 [3 Jan 2017]
 */
public class XhrHandler extends AbstractSessionHandler {
	private static final long serialVersionUID = -527374807374550532L;
	private static final String CONTENT_TYPE_VALUE = "application/javascript;charset=UTF-8";

	/**
	 * 
	 * @param ctx
	 */
	public XhrHandler(Servlet servlet) {
		super(servlet, CONTENT_TYPE_VALUE, true, "POST");
	}

	@Override
	protected void handle(HttpAsyncContext async, ServletSession session, boolean initial)
	throws ServletException, IOException
	{
		final Pipe pipe = Pipe.open();
		final WritableByteChannel dest = pipe.sink();
		async.getResponse().getOutputStream().setWriteListener(new WriteStream(async, pipe.source()));

		if (initial) {
			dest.write(UTF_8.encode(CharBuffer.wrap("o\n")));
			dest.close();
		} else {
			try (Sender sender = new XhrSender(session, dest)) {
				session.setSender(sender);
			}
		}
	}


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [18 Feb 2017]
	 */
	private class XhrSender implements Sender {
		private final ServletSession session;
		private final WritableByteChannel dest;
		private boolean first = true;

		public XhrSender(ServletSession session, WritableByteChannel dest) {
			this.session = session;
			this.dest = dest;
		}

		@Override
		public void send(String frame, boolean last) throws IOException {
			final CharBuffer buf = CharBuffer.allocate(frame.length() + 6);
			if (this.first) {
				buf.append("a[\"");
				this.first = !this.first;
			}
			buf.append(StringEscapeUtils.escapeJson(frame));
			buf.append(last ? "\"]\n" : "\",\"");
			buf.flip();
			this.dest.write(UTF_8.encode(buf));
		}

		@Override
		public void close() throws IOException {
			this.session.setSender(null);
			this.dest.close();
		}
	}
}
