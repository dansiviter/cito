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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.channels.Pipe;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import cito.sockjs.nio.ReadStream;

/**
 * Handles JSONP Send ({@code /<server>/<session>/jsonp_send}) connections.
 * 
 * @author Daniel Siviter
 * @since v1.0 [1 Mar 2017]
 */
public class JsonPSendHandler extends AbstractSessionHandler {
	private static final long serialVersionUID = 6883526585964051391L;

	static final String JSONP_SEND = "jsonp_send";

	private static final String CONTENT_TYPE_VALUE = "text/plain;charset=UTF-8";

	/**
	 * @param servlet
	 */
	public JsonPSendHandler(Servlet servlet) {
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
			this.log.warn("Session not found! [{}]", Util.session(this.servlet, async.getRequest()));
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

		final HttpServletRequest req = async.getRequest();
		try (Reader reader = newReader(src, UTF_8.newDecoder(), -1)) {
			if (req.getContentType().startsWith(MediaType.APPLICATION_FORM_URLENCODED)) {
				final MultivaluedMap<String, String> formData = parseFormData(new BufferedReader(reader));

				final List<String> data = formData.get("d");

				System.out.println("data: " + data);
				
				if (data == null || data.isEmpty()) {
					this.log.warn("No data!");
					sendNonBlock(async, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Payload expected.");
					return;
				}
				for (String payload : data) {
					payload = StringEscapeUtils.unescapeEcmaScript(payload);
					read(session, async, new StringReader(payload));
				}
			} else {
				read(session, async, reader);
			}
			sendNonBlock(async, HttpServletResponse.SC_OK, "ok");
		} catch (IOException | JsonException e) {
			final String message = e instanceof JsonException ? "Broken JSON encoding." : "Error processing data!";
			this.log.warn(message, e);
			sendNonBlock(async, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
		}
	}

	/**
	 * 
	 * @param session
	 * @param async
	 * @param payloads
	 * @throws IOException
	 */
	private void read(ServletSession session, HttpAsyncContext async, Reader reader) throws IOException {
		try (JsonParser parser = Json.createParser(reader)) {
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
		}
	}


	// --- Static Methods ---

	/**
	 * 
	 * @param reader
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private static MultivaluedMap<String, String> parseFormData(BufferedReader reader) throws UnsupportedEncodingException, IOException {
		final MultivaluedMap<String, String> result = new MultivaluedHashMap<>();
		String line;
		while ((line = reader.readLine()) != null) {
			String[] pairs = line.split("&");
			for (String pair : pairs) {
				if (StringUtils.isEmpty(pair)) {
					continue;
				}
				pair = URLDecoder.decode(pair, UTF_8.name());
				final int i = pair.indexOf('=');
				if (i == -1) {
					result.add(pair, null);
				} else {
					final String value = pair.substring(i + 1);
					if (StringUtils.isEmpty(value)) {
						continue;
					}
					result.add(pair.substring(0, i), value);
				}
			}
		}
		return result;
	}
}
