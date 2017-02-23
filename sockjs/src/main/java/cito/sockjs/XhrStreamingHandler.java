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
import java.nio.channels.Pipe;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import cito.sockjs.nio.WriteStream;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [3 Jan 2017]
 */
public class XhrStreamingHandler extends AbstractSessionHandler {
	private static final long serialVersionUID = -527374807374550532L;
	private static final byte[] SEPARATOR = "\n".getBytes(StandardCharsets.UTF_8);
	private static final String CONTENT_TYPE_VALUE = "application/javascript; charset=UTF-8";
	private static final String PRELUDE = StringUtils.leftPad("", 2048, "h");

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

		if (initial) {
			final ServletOutputStream out = async.getResponse().getOutputStream();
			out.write(OPEN_FRAME);
			out.write(SEPARATOR);
			async.complete();
			return;
		}

		final Pipe pipe = Pipe.open();
		session.setSender(new XhrStreamingSender(session, pipe.sink()));
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
		private boolean first = true;
		private int bytesSent;
		private final ByteBuffer buffer = ByteBuffer.allocate(5);

		public XhrStreamingSender(ServletSession session, SinkChannel dest) throws IOException {
			this.session = session;
			this.dest = dest;
			this.dest.write(toByteBuffer(PRELUDE));
		}

		@Override
		public void send(String frame, boolean last) throws IOException {
			if (this.first) {
				this.first = !this.first;
				write(toByteBuffer("a["));
			} else {
				write(toByteBuffer(","));
			}
			if (write(toByteBuffer(StringEscapeUtils.escapeJson(frame)))) {
				return;
			}

			if (last) {
				write(toByteBuffer("]\n"));
				this.first = true;
			}
		}
	
		/**
		 * 
		 * @param buf
		 * @return
		 * @throws IOException
		 */
		private boolean write(ByteBuffer buf) throws IOException {
			this.dest.write(buf);
			if ((bytesSent += buf.capacity()) > servlet.ctx.getConfig().maxStreamBytes()) {
				servlet.log("Limit to streaming bytes reached. Closing sender.");
				close();
				return false;
			}
			return true;
		}

		/**
		 * 
		 * @param str
		 * @return
		 */
		private ByteBuffer toByteBuffer(String str) {
			buffer.reset();
			buffer.put(str.getBytes(UTF_8));
			return buffer;
		}


		@Override
		public void close() throws IOException {
			this.dest.write(toByteBuffer("]\n"));
			this.session.setSender(null);
		}
	}
}
