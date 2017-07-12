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

import static java.nio.channels.Channels.newReader;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.channels.Pipe;
import java.nio.channels.ReadableByteChannel;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;

import cito.sockjs.nio.ReadStream;

/**
 * Handles XHR Send ({@code /<server>/<session>/xhr_send}) connections.
 * 
 * @author Daniel Siviter
 * @since v1.0 [11 Feb 2017]
 */
public class XhrSendHandler extends AbstractSessionHandler {
	static final String XHR_SEND = "xhr_send";
	private static final String CONTENT_TYPE_VALUE = "text/plain;charset=UTF-8";

	/**
	 * @param servlet
	 */
	public XhrSendHandler(Servlet servlet) {
		super(servlet, CONTENT_TYPE_VALUE, false, "POST");
	}

	@Override
	protected void handle(HttpAsyncContext async, ServletSession session, boolean initial)
	throws ServletException, IOException
	{
		final HttpServletRequest req = async.getRequest();

		if (req.getContentLength() <= 0) {
			this.log.warn("Payload expected.");
			sendNonBlock(async, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Payload expected.");
			return;
		}
		if (session == null) {
			if (this.log.isWarnEnabled()) {
				this.log.warn("Session not found! [{}]", Util.session(this.servlet, async.getRequest()));
			}
			sendNonBlock(async, HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		final Pipe pipe = Pipe.open();
		// FIXME Not strictly non-blocking as it's still reading off another thread which is blocked
		async.start(() -> start(session, async, pipe.source()));
		req.getInputStream().setReadListener(new ReadStream(async, pipe.sink(), t -> pipe.sink().close()));
	}

	/**
	 * 
	 * @param session
	 * @param async
	 * @param src
	 */
	public void start(ServletSession session, HttpAsyncContext async, ReadableByteChannel src) {
		try (JsonParser parser = Json.createParser(newReader(src, UTF_8.newDecoder(), -1))) {
			while (parser.hasNext()) {
				final Event evt = parser.next();
				switch (evt) {
				case START_ARRAY:
					continue;
				case END_ARRAY:
					break;
				case VALUE_STRING:
					final String value = parser.getString();
					session.forwardMessage(StringEscapeUtils.unescapeJson(value));
					continue;
				default:
					throw new JsonException("Only Array Start/End and String expected!");
				}
			}
			async.getResponse().setStatus(HttpServletResponse.SC_NO_CONTENT);
			async.complete();
		} catch (IOException | JsonException e) {
			final String message = e instanceof JsonException ? "Broken JSON encoding." : "Error processing data!";
			this.log.warn(message, e);
			sendNonBlock(async, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
		}
	}
}
