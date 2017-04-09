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

import javax.servlet.ServletException;

import org.apache.commons.lang3.StringUtils;

/**
 * Handles XHR Streaming ({@code /<server>/<session>/xhr_streaming}) connections.
 * 
 * @author Daniel Siviter
 * @since v1.0 [3 Jan 2017]
 */
public class XhrStreamingHandler extends AbstractStreamingHandler {
	private static final long serialVersionUID = -527374807374550532L;

	static final String XHR_STREAMING = "xhr_streaming";
	private static final String CONTENT_TYPE_VALUE = "application/javascript;charset=UTF-8";
	private static final String PRELUDE = StringUtils.leftPad("\n", 2049, "h");

	/**
	 * 
	 * @param ctx
	 */
	public XhrStreamingHandler(Servlet servlet) {
		super(servlet, CONTENT_TYPE_VALUE, "POST");
	}

	@Override
	protected void handle(HttpAsyncContext async, ServletSession session, boolean initial)
	throws ServletException, IOException
	{
		handle(async, session, initial, DEFAULT_FORMAT, () -> PRELUDE);
	}
}
