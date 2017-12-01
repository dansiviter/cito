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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [3 Jan 2017]
 */
public enum Util { ;
	/**
	 * 
	 * @param config
	 * @param req
	 * @return
	 */
	private static String[] uriTokens(Config config, HttpServletRequest req) {
		// removes '/<path>/'
		final String[] tokens = req.getRequestURI().substring(config.path().length() + 2).split("/");
		if (tokens.length != 3) {
			throw new IllegalStateException("Invalid path! [" + req.getRequestURI() + "]");
		}
		return tokens;
	}

	/**
	 * 
	 * @param config
	 * @param req
	 * @return
	 */
	public static String session(Servlet servlet, HttpServletRequest req) {
		return uriTokens(servlet.getConfig(), req)[1];
	}

	/**
	 * 
	 * @param config
	 * @param req
	 * @return
	 */
	public static String session(Config config, HttpServletRequest req) {
		return uriTokens(config, req)[1];
	}

	/**
	 * 
	 * @param r
	 * @return
	 */
	public static String server(Config config, HttpServletRequest req) {
		return uriTokens(config, req)[0];
	}

	/**
	 * 
	 * @param r
	 * @return
	 */
	public static String type(Config config, HttpServletRequest req) {
		return uriTokens(config, req)[2];
	}

	/**
	 * 
	 * @param r
	 * @return
	 */
	public static Map<String, String> pathParams(Config config, HttpServletRequest req) {
		final Map<String, String> pathParams = new HashMap<>();
		pathParams.put("session", session(config, req));
		pathParams.put("server", server(config, req));
		return Collections.unmodifiableMap(pathParams);
	}

	/**
	 * 
	 * @param cs
	 * @return
	 */
	public static boolean isEmptyOrNull(CharSequence cs) {
		return isEmptyOrNull(cs, false);
	}

	/**
	 * 
	 * @param cs
	 * @param trim
	 * @return
	 */
	public static boolean isEmptyOrNull(CharSequence cs, boolean trim) {
		return cs == null || (trim ? cs.toString().trim() : cs).length() == 0;
	}

	/**
	 * 
	 * @param async
	 * @return
	 */
	public static ServletContext servletContext(HttpAsyncContext async) {
		return async.getRequest().getServletContext();
	}

	/**
	 * 
	 * @param cls
	 * @param name
	 * @return
	 * @throws IOException
	 */
	public static ByteBuffer resourceToByteBuffer(Class<?> cls, String name) throws IOException {
		try (ReadableByteChannel in = Channels.newChannel(cls.getResourceAsStream(name));
				ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
				WritableByteChannel out = Channels.newChannel(os))
		{
			final ByteBuffer buf = ByteBuffer.allocate(1024);
			while (in.read(buf) > 0) {
				buf.flip();
				out.write(buf);
				buf.clear();
			}
			return ByteBuffer.wrap(os.toByteArray());
		}
	}

	/**
	 * 
	 * @param cls
	 * @param name
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	public static String resourceToString(Class<?> cls, String name, Charset charset) throws IOException {
		return charset.decode(resourceToByteBuffer(cls, name)).toString();
	}
}
