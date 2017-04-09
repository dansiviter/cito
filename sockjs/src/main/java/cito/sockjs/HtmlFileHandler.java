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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Handles HTML File Streaming ({@code /<server>/<session>/htmlfile}) connections.
 * 
 * @author Daniel Siviter
 * @since v1.0 [1 Mar 2017]
 */
public class HtmlFileHandler extends AbstractStreamingHandler {
	private static final long serialVersionUID = -4614348605938993415L;

	static final String HTMLFILE = "htmlfile";

	private static final String PRELUDE;
	static {
		try {
			final String htmlfile = Util.resourceToString(HtmlFileHandler.class, "htmlfile.html");
			PRELUDE = new StringBuilder(StringUtils.rightPad(htmlfile, 1_024)).append("\r\n").toString();
		} catch (IOException e) {
			throw new IllegalStateException("Unable to load template!", e);
		}
	}

	/**
	 * 
	 * @param servlet
	 */
	public HtmlFileHandler(Servlet servlet) {
		super(servlet, "text/html;charset=UTF-8", "GET");
	}

	@Override
	protected void handle(HttpAsyncContext async, ServletSession session, boolean initial)
	throws ServletException, IOException
	{
		final String callback = getCallback(async.getRequest());
		if (callback == null || callback.isEmpty()) {
			this.log.warn("Callback expected.");
			sendNonBlock(async, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "\"callback\" parameter required");
			return;
		}

		handle(async, session, initial, HtmlFileHandler::format, () -> String.format(PRELUDE, callback));
	}


	// --- Static Methods ---

	/**
	 * 
	 * @param frame
	 * @param callback
	 * @return
	 */
	private static CharSequence format(CharSequence frame) {
		return new StringBuilder("<script>\np(\"")
				.append(StringEscapeUtils.ESCAPE_ECMASCRIPT.translate(frame))
				.append("\");\n</script>\r\n");
	}

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
}
