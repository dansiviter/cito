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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import cito.sockjs.nio.WriteStream;

/**
 * @author Daniel Siviter
 * @since v1.0 [1 Mar 2017]
 */
public class HtmlFileHandler extends AbstractSessionHandler {
	private static final long serialVersionUID = -4614348605938993415L;

	static final String HTMLFILE = "htmlfile";

	private byte[] template;

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
			this.template = Util.resource(getClass(), "htmlfile.html");
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

		final ReadableByteChannel htmlFileChannel = Channels.newChannel(new ByteArrayInputStream(this.template));
		res.getOutputStream().setWriteListener(new WriteStream(async, htmlFileChannel, t -> {
			async.complete();
			htmlFileChannel.close();

			if (t != null) {
				async.getRequest().getServletContext().log("Unable to read entity!", t);
			}
		}));
	}
}
