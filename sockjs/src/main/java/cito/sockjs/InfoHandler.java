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
import java.security.SecureRandom;
import java.util.Random;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Dec 2016]
 */
public class InfoHandler extends AbstractHandler {
	private static final long serialVersionUID = 4503408709378376273L;
	static final String INFO = "info";

	protected static final Logger LOG = LoggerFactory.getLogger(InfoHandler.class);

	private static final Random RANDOM = new SecureRandom();

	/**
	 * 
	 * @param servlet
	 */
	public InfoHandler(Servlet servlet) {
		super(servlet, "application/json;charset=UTF-8", "GET");
	}

	@Override
	protected void handle(HttpAsyncContext async) throws ServletException, IOException {
		try (JsonGenerator generator = Json.createGenerator(async.getResponse().getWriter())) { // FIXME blocking
			createJson(generator, generateEntropy(), true, this.servlet.isWebSocketSupported(), "*:*");
		}
		async.complete();
	}

	/**
	 * Creates JSON payload:
	 * <pre>
	 * {
	 *   "entropy": -1261588729,
	 *   "origins": [
	 *     "*:*"
	 *   ],
	 *   "cookie_needed": true,
	 *   "websocket": true
	 * }
	 * </pre>
	 * 
	 * @return
	 */
	private static void createJson(JsonGenerator generator, long entropy, boolean cookieNeeded, boolean websocket, String... origins) {
		generator.writeStartObject();
		generator.write("entropy", entropy);
		if (origins.length > 0) { // currently ignored
			generator.writeStartArray("origins");
			for (String origin : origins) {
				generator.write(origin);
			}
			generator.writeEnd();
		}
		generator.write("cookie_needed", cookieNeeded);
		generator.write("websocket", websocket);
		generator.writeEnd();
	}

	/**
	 * 
	 * @return
	 */
	public static long generateEntropy() {
		return RANDOM.nextLong();
	}
}
