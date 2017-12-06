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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.channels.Pipe;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.StringJoiner;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Siviter
 * @since v1.0 [12 Feb 2017]
 */
public abstract class AbstractHandler {
	protected static final String CORS_ORIGIN = "Access-Control-Allow-Origin";
	protected static final String CORS_CREDENTIALS = "Access-Control-Allow-Credentials";
	protected static final String CORS_REQUEST_HEADERS = "Access-Control-Request-Headers";
	protected static final String CORS_ALLOW_HEADERS = "Access-Control-Allow-Headers";

	protected final Logger log;
	protected final Servlet servlet;
	protected final String mediaType;
	protected final String[] methods;

	/**
	 * 
	 * @param servlet
	 * @param mediaType
	 * @param methods
	 */
	public AbstractHandler(Servlet servlet, String mediaType, String... methods) {
		this.log = LoggerFactory.getLogger(getClass());
		this.servlet = servlet;
		this.mediaType = mediaType;
		this.methods = methods;
	}

	/**
	 * 
	 * @return
	 * @throws ServletException
	 */
	public AbstractHandler init() throws ServletException { 
		return this;
	}

	/**
	 * 
	 * @param asyncCtx
	 * @throws ServletException
	 * @throws IOException
	 */
	public void service(HttpAsyncContext async) throws ServletException, IOException {
		final String method = async.getRequest().getMethod();
		if ("OPTIONS".equals(method)) {
			options(async, ArrayUtils.add(this.methods, "OPTIONS"));
			return;
		}
		if (!ArrayUtils.contains(this.methods, method)) {
			sendNonBlock(async, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}

		async.getResponse().setContentType(this.mediaType);
		setCors(async.getRequest(), async.getResponse());
		setCacheControl(async);

		handle(async);
	}

	/**
	 * 
	 * @param async
	 * @throws ServletException
	 * @throws IOException
	 */
	protected abstract void handle(HttpAsyncContext async)
			throws ServletException, IOException;

	/**
	 * 
	 * @param req
	 * @param res
	 * @param methods
	 */
	protected void options(HttpServletRequest req, HttpServletResponse res, String... methods) {
		res.setStatus(HttpServletResponse.SC_NO_CONTENT);
		res.setHeader("Cache-Control", "public, max-age=31536000"); // one year
		res.setDateHeader("Expires", ZonedDateTime.now(ZoneOffset.UTC.normalized()).plusYears(1).toEpochSecond());
		res.setIntHeader("Access-Control-Max-Age", 1000001);
		final StringJoiner joiner = new StringJoiner(", ");
		Arrays.asList(methods).forEach(joiner::add);
		res.setHeader("Access-Control-Allow-Methods", joiner.toString());
		setCors(req, res);
	}

	/**
	 * 
	 * @param asyncCtx
	 * @param methods
	 */
	protected void options(HttpAsyncContext asyncCtx, String... methods) {
		options(asyncCtx.getRequest(),  asyncCtx.getResponse(), methods);
		asyncCtx.complete();
	}

	/**
	 * 
	 * @param async
	 * @param statusCode
	 * @throws IOException
	 */
	protected void sendNonBlock(HttpAsyncContext async, int statusCode) throws IOException {
		sendNonBlock(async, statusCode, null);
	}

	/**
	 * 
	 * @param async
	 * @param statusCode
	 * @param message
	 */
	protected void sendNonBlock(HttpAsyncContext async, int statusCode, String message) {
		this.log.info("Completing async. [uri={},status={},msg={}]", async.getRequest().getRequestURI(), statusCode, message);
		try {
			final HttpServletResponse res = async.getResponse();
			res.setStatus(statusCode);

			if (Util.isEmptyOrNull(message)) {
				async.complete();
				return;
			}

			final ServletOutputStream os = res.getOutputStream();
			os.setWriteListener(new WriteListener() {
				@Override
				public void onWritePossible() throws IOException {
					os.print(message);
					async.complete();
				}

				@Override
				public void onError(Throwable t) {
					log.error("Unable to write error!", t);
					async.complete();
				}
			});
		} catch (IOException e) {
			this.log.error("Unable to write error!", e);
			async.complete();
		}
	}

	/**
	 * 
	 * @param pipe
	 * @param s
	 * @return
	 * @throws IOException
	 */
	protected int write(Pipe pipe, CharSequence s) throws IOException {
		if (s == null || s.length() == 0) {
			return 0;
		}
		return pipe.sink().write(UTF_8.encode(CharBuffer.wrap(s)));
	}

	/**
	 * 
	 * @param pipe
	 * @param s0
	 * @param s1
	 * @return
	 * @throws IOException
	 */
	protected int write(Pipe pipe, CharSequence s0, CharSequence s1) throws IOException {
		return write(pipe, s0) + write(pipe, s1);
	}

	/**
	 * 
	 * @param pipe
	 * @param s0
	 * @param s1
	 * @param s2
	 * @return
	 * @throws IOException
	 */
	protected int write(Pipe pipe, CharSequence s0, CharSequence s1, CharSequence s2) throws IOException {
		return write(pipe, s0, s1) + write(pipe, s2);
	}

	/**
	 * Convenience method to write lots of data to the pipe.
	 * <p/>
	 * Creates garbage as the vararg will create a new array regardless.
	 * 
	 * @param pipe
	 * @param sequences
	 * @return
	 * @throws IOException
	 */
	protected int write(Pipe pipe, CharSequence... sequences) throws IOException {
		int bytes = 0;
		for (CharSequence s : sequences) {
			bytes += write(pipe, s);
		}
		return bytes;
	}


	/// --- Static Methods ---

	/**
	 * 
	 * @param req
	 * @param res
	 */
	protected static void setCors(HttpServletRequest req, HttpServletResponse res) {
		final String origin = req.getHeader("Origin");
		res.setHeader(CORS_ORIGIN, origin == null ? "*" : origin);
		res.setHeader(CORS_CREDENTIALS, Boolean.TRUE.toString());

		final String headers = req.getHeader(CORS_REQUEST_HEADERS);
		if (headers != null && !headers.isEmpty()) {
			res.setHeader(CORS_ALLOW_HEADERS, headers);
		}
	}

	/**
	 * 
	 * @param res
	 */
	protected static void setCacheControl(HttpServletResponse res) {
		res.setHeader(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate, max-age=0");
	}

	/**
	 * 
	 * @param asyncCtx
	 */
	protected static void setCacheControl(HttpAsyncContext asyncCtx) {
		setCacheControl(asyncCtx.getResponse());
	}

	/**
	 * 
	 * @param code
	 * @param message
	 * @return
	 */
	protected static String closeFrame(int code, String message) {
		return new StringBuilder("c[")
				.append(code).append(",\"")
				.append(message).append("\"]")
				.toString();
	}
}
