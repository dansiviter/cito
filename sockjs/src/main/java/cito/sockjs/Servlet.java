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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.AsyncContext;
import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [4 Jan 2017]
 */
public class Servlet extends GenericServlet {
	private static final long serialVersionUID = 917110139775886906L;

	private final Map<String, AbstractHandler> handers = new HashMap<>();

	protected final Context ctx;

	protected Servlet(Context ctx) {
		this.ctx = ctx;
	}

	@Override
	public void init() throws ServletException {
		this.handers.put("iframe", new IFrameHandler(this).init());
		this.handers.put("info", new InfoHandler(this).init());
		this.handers.put("xhr", new XhrHandler(this).init());
		this.handers.put("xhr_send", new XhrSendHandler(this).init());
	}

	@Override
	public void service(ServletRequest req, ServletResponse res)
			throws ServletException, IOException
	{
		HttpServletRequest  request;
		HttpServletResponse response;

		if (!(req instanceof HttpServletRequest &&
				res instanceof HttpServletResponse)) {
			throw new ServletException("non-HTTP request or response");
		}

		request = (HttpServletRequest) req;
		response = (HttpServletResponse) res;

		service(request, response);
	}

	/**
	 * 
	 * @param req
	 * @param res
	 * @throws ServletException
	 * @throws IOException
	 */
	private void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		final String fullPath = req.getRequestURI() + (req.getQueryString() != null ? "?" + req.getQueryString() : "");
		log("SockJS request recieved. [path=" + fullPath + ",method=" + req.getMethod() + "]");

		final String path = req.getRequestURI().substring(this.ctx.getConfig().path().length() + 2);
		final String[] segments = path.split("/");

		String type = null;
		if (segments.length == 1) {
			type = segments[0];
			if (type.startsWith("iframe")) {
				type = "iframe"; // special case, avoids a regex or similar
			}
		} else if (segments.length == 3) {
			type = segments[2];
		}

		final AbstractHandler handler = this.handers.get(type);
		if (handler == null) {
			log("Invalid path sent to SockJS! [path=" + fullPath + ",method=" + req.getMethod() + "]");
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		final AsyncContext asyncContext = req.startAsync();
		asyncContext.setTimeout(TimeUnit.MINUTES.toMillis(1));
		asyncContext.start(() -> onRequest(handler, asyncContext));
	}

	/**
	 * 
	 * @param req
	 * @return
	 * @throws ServletException
	 */
	protected ServletSession getSession(HttpServletRequest req) throws ServletException {
		final String sessionId = Util.session(this.ctx.getConfig(), req);
		return  this.ctx.getSession(sessionId);
	}

	/**
	 * 
	 * @param req
	 * @return
	 * @throws ServletException 
	 */
	protected ServletSession createSession(HttpServletRequest req) throws ServletException {
		final ServletSession session = new ServletSession(this, req);
		this.ctx.register(session);
		session.getEndpoint().onOpen(session, this.ctx.getConfig());
		return session;
	}

	/**
	 * 
	 * @param handler
	 * @param asyncCtx
	 */
	private void onRequest(AbstractHandler handler, AsyncContext asyncCtx) {
		final HttpAsyncContext httpAsyncContext = new HttpAsyncContext(asyncCtx);
		try {
			handler.service(httpAsyncContext);
		} catch (ServletException | IOException | RuntimeException e) {
			onError(e, asyncCtx);
		}
	}

	/**
	 * 
	 * @param t
	 * @param ctx
	 */
	private void onError(Throwable t, AsyncContext asyncCtx) {
		log("Error while servicing request!", t);
		((HttpServletResponse) asyncCtx.getResponse()).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		asyncCtx.complete();
	}
}
