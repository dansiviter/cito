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

/**
 * Handles EventSource ({@code <server>/<session>/eventsource}) connections.
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Feb 2017]
 */
public class EventSourceHandler extends AbstractStreamingHandler {
	private static final long serialVersionUID = -527374807374550532L;
	
	private static final FrameFormat ES_FORMAT = c -> new StringBuilder("data: ").append(c).append("\r\n\r\n");
	
	static final String EVENTSOURCE = "eventsource";
	private static final String CONTENT_TYPE_VALUE = "text/event-stream;charset=UTF-8";
	private static final String PRELUDE = "\r\n";

	/**
	 * 
	 * @param ctx
	 */
	public EventSourceHandler(Servlet servlet) {
		super(servlet, CONTENT_TYPE_VALUE, "GET");
	}

	@Override
	protected void handle(HttpAsyncContext async, ServletSession session, boolean initial)
	throws ServletException, IOException
	{
		handle(async, session, initial, ES_FORMAT, () -> PRELUDE);
	}
}
