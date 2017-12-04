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

import static cito.sockjs.EventSourceHandler.EVENTSOURCE;
import static cito.sockjs.GreetingHandler.GREETING;
import static cito.sockjs.HtmlFileHandler.HTMLFILE;
import static cito.sockjs.IFrameHandler.IFRAME;
import static cito.sockjs.InfoHandler.INFO;
import static cito.sockjs.JsonPHandler.JSONP;
import static cito.sockjs.JsonPSendHandler.JSONP_SEND;
import static cito.sockjs.XhrHandler.XHR;
import static cito.sockjs.XhrSendHandler.XHR_SEND;
import static cito.sockjs.XhrStreamingHandler.XHR_STREAMING;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.Endpoint;
import javax.websocket.Extension;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The link between SockJS and the servlet container. See {@link Config} for usage.
 * 
 * @author Daniel Siviter
 * @since v1.0 [4 Jan 2017]
 */
public class Servlet implements javax.servlet.Servlet, WebSocketContainer {
	private final Map<String, AbstractHandler> handers = new HashMap<>();
	private final Map<String, ServletSession> sessions = new ConcurrentHashMap<>();
	// XXX Should I use ManagedScheduledExecutorService?
	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

	private final Logger log = LoggerFactory.getLogger(Servlet.class);
	private final Config config;

	private ServletConfig servletConfig;
	private boolean webSocketSupported;

	protected Servlet(Config config) {
		this.config = config;
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		this.servletConfig = servletConfig;
		this.handers.put(GREETING, new GreetingHandler(this).init());
		this.handers.put(IFRAME, new IFrameHandler(this).init());
		this.handers.put(INFO, new InfoHandler(this).init());
		this.handers.put(XHR, new XhrHandler(this).init());
		this.handers.put(XHR_SEND, new XhrSendHandler(this).init());
		this.handers.put(XHR_STREAMING, new XhrStreamingHandler(this).init());
		this.handers.put(EVENTSOURCE, new EventSourceHandler(this).init());
		this.handers.put(HTMLFILE, new HtmlFileHandler(this).init());
		this.handers.put(JSONP, new JsonPHandler(this).init());
		this.handers.put(JSONP_SEND, new JsonPSendHandler(this).init());

		this.scheduler.scheduleWithFixedDelay(this::cleanupSessions, 5, 5, TimeUnit.SECONDS);
	}

	@Override
	public ServletConfig getServletConfig() {
		return this.servletConfig;
	}

	@Override
	public String getServletInfo() {
		return "Citō SockJS";
	}

	/**
	 * @return the config
	 */
	public Config getConfig() {
		return config;
	}

	/**
	 * @return the webSocketSupported
	 */
	public boolean isWebSocketSupported() {
		return webSocketSupported;
	}

	/**
	 * 
	 * @param supported
	 */
	void setWebSocketSupported(boolean supported) {
		this.webSocketSupported = supported;
	}

	@Override
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		if (!(req instanceof HttpServletRequest && res instanceof HttpServletResponse)) {
			throw new ServletException("non-HTTP request or response");
		}
		service((HttpServletRequest) req, (HttpServletResponse) res);
	}

	/**
	 * 
	 * @param req
	 * @param res
	 * @throws ServletException
	 * @throws IOException
	 */
	private void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		this.log.info("SockJS request recieved. [name={},path={},method={}]", this.config.name(), req.getRequestURI(), req.getMethod());

		String type = null;
		if (req.getPathTranslated() == null) {
			type = GREETING;
		} else {
			final String[] segments = req.getPathInfo().substring(1).split("/"); // strip leading '/'
			if (segments.length == 1) {
				type = segments[0].toLowerCase();
				if (type.startsWith(IFRAME)) {
					type = IFRAME; // special case, avoids a regex or similar
				}
			} else if (segments.length == 3) {
				type = segments[2];
			}
		}

		final AbstractHandler handler = this.handers.get(type);
		if (handler == null) {
			this.log.warn("Invalid path sent to SockJS! [name={},path={},method={}]", this.config.name(), req.getRequestURI(), req.getMethod());
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
		final String sessionId = Util.session(this.config, req);
		final ServletSession session = this.sessions.get(sessionId);
		return session;
	}

	/**
	 * 
	 * @param req
	 * @return
	 * @throws ServletException 
	 */
	protected ServletSession createSession(HttpServletRequest req) throws ServletException {
		final ServletSession session = new ServletSession(this, req);
		if (this.sessions.putIfAbsent(session.getId(), session) != null) {
			throw new ServletException("Session already exists! [" + Util.session(this.config, req) + "]");
		}
		session.getEndpoint().onOpen(session, this.config);
		return session;
	}

	/**
	 * 
	 * @param session
	 */
	public void unregister(ServletSession session) {
		final String id = session.getId();
		// if super old, remove straight away
		if (session.activeTime().isBefore(LocalDateTime.now().minus(5, ChronoUnit.SECONDS))) {
			this.log.debug("Removing session straight away. [{}]", id);
			this.sessions.remove(id);
			return;
		}

		this.scheduler.schedule(() -> {
			this.log.debug("Removing session after delay. [{}]", id);
			this.sessions.remove(id);
		}, 5, TimeUnit.SECONDS); 
	}

	/**
	 * 
	 * @param handler
	 * @param asyncCtx
	 */
	private void onRequest(AbstractHandler handler, AsyncContext asyncCtx) {
		final HttpAsyncContext async = new HttpAsyncContext(asyncCtx);
		try {
			handler.service(async);
		} catch (ServletException | IOException | RuntimeException e) {
			onError(e, async);
		}
	}

	/**
	 * 
	 * @param t
	 * @param async
	 */
	private void onError(Throwable t, HttpAsyncContext async) {
		this.log.warn("Error while servicing request!", t);
		async.getResponse().setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		async.complete();
	}

	/**
	 * 
	 */
	private void cleanupSessions() {
		this.log.info("Cleaning up inactive sessions. [count={}]", this.sessions.size());
		this.sessions.forEach((k, v) -> {
			final String id = v.getId();
			if (v.isOpen() && v.activeTime().isBefore(LocalDateTime.now().plus(5, ChronoUnit.SECONDS))) {
				try {
					v.close();
				} catch (IOException e) {
					this.log.warn("Error closing session! [" + id + "]", e);
				}
			}
		});
	}

	@Override
	public void destroy() { }

	/* (non-Javadoc)
	 * @see javax.websocket.WebSocketContainer#getDefaultAsyncSendTimeout()
	 */
	@Override
	public long getDefaultAsyncSendTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see javax.websocket.WebSocketContainer#setAsyncSendTimeout(long)
	 */
	@Override
	public void setAsyncSendTimeout(long timeoutmillis) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Session connectToServer(Object annotatedEndpointInstance, URI path) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Session connectToServer(Class<?> annotatedEndpointClass, URI path) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Session connectToServer(Endpoint endpointInstance, ClientEndpointConfig cec, URI path) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Session connectToServer(Class<? extends Endpoint> endpointClass, ClientEndpointConfig cec, URI path) {
		throw new UnsupportedOperationException();

	}

	@Override
	public long getDefaultMaxSessionIdleTimeout() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDefaultMaxSessionIdleTimeout(long timeout) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getDefaultMaxBinaryMessageBufferSize() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDefaultMaxBinaryMessageBufferSize(int max) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getDefaultMaxTextMessageBufferSize() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDefaultMaxTextMessageBufferSize(int max) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Extension> getInstalledExtensions() {
		throw new UnsupportedOperationException();
	}
}
