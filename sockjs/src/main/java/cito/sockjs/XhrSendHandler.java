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
import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.lang3.StringEscapeUtils;

import cito.sockjs.nio.ReadStream;


/**
 * @author Daniel Siviter
 * @since v1.0 [11 Feb 2017]
 */
public class XhrSendHandler extends AbstractHandler {
	private static final long serialVersionUID = 8893825977852213991L;

	private static final String CONTENT_TYPE_VALUE = "text/plain; charset=UTF-8";

	/**
	 * @param servlet
	 */
	public XhrSendHandler(Servlet servlet) {
		super(servlet);
	}

	@Override
	public void service(HttpAsyncContext asyncCtx) throws ServletException, IOException {
		final HttpServletRequest req = asyncCtx.getRequest();
		final HttpServletResponse res = asyncCtx.getResponse();

		if ("OPTIONS".equals(req.getMethod())) {
			options(asyncCtx, "OPTIONS", "POST");
			return;
		}
		if (!"POST".equals(req.getMethod())) {
			sendErrorNonBlock(asyncCtx, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}

		final ServletSession session = this.servlet.getSession(req);
		if (session == null) {
			this.servlet.log("Session not found! [" + Util.session(this.servlet, req) + "]");
			sendErrorNonBlock(asyncCtx, HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		res.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_VALUE);
		setCors(req, res);
		setCacheControl(asyncCtx);

		if (req.getContentLength() <= 0) {
			this.servlet.log("Payload expected.");
			sendErrorNonBlock(asyncCtx, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Payload expected.");
			return;
		}
		
		final Pipe pipe = Pipe.open();
		asyncCtx.start(() -> start(session, asyncCtx, pipe.source()));
		req.getInputStream().setReadListener(new ReadStream(asyncCtx, pipe.sink(), () -> pipe.sink().close()));
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
					send(session, StringEscapeUtils.unescapeJson(value));
					continue;
				default:
					throw new JsonException("Only Array Start/End and String expected!");
				}
			}
			async.getResponse().setStatus(HttpServletResponse.SC_NO_CONTENT);
			async.complete();
		} catch (IOException | JsonException e) {
			final String message = e instanceof JsonException ? "Broken JSON encoding." : "Error processing data!";
			sendErrorNonBlock(async, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
		}
	}
}
