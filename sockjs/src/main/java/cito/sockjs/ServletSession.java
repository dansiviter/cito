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
import java.net.URI;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedTransferQueue;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.RemoteEndpoint.Basic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [3 Jan 2017]
 */
public class ServletSession extends AbstractSession {
	protected static final String FRAME_DELIMITER = "\n";

	private final LinkedTransferQueue<String> frameQueue = new LinkedTransferQueue<>();

	private final Servlet servlet;
	private final HttpServletRequest instigatingReq;
	private final Endpoint endpoint;
	private final Map<String, String> pathParams;

	private Basic basic;
	private LocalDateTime active, closed;
	private volatile Sender sender;

	/**
	 * 
	 * @param servlet
	 * @param instigatingReq
	 * @throws ServletException
	 */
	public ServletSession(Servlet servlet, HttpServletRequest instigatingReq)
			throws ServletException
	{
		this.servlet = servlet;
		this.instigatingReq = instigatingReq;
		this.endpoint = servlet.getConfig().createEndpoint();
		this.pathParams = Util.pathParams(servlet.getConfig(), instigatingReq);
		this.active = LocalDateTime.now();
	}

	/**
	 * @return the endpoint
	 */
	public Endpoint getEndpoint() {
		return endpoint;
	}

	/**
	 * 
	 */
	protected Basic createBasic() {
		return new DefaultBasic();
	}

	@Override
	public String getId() {
		return getPathParameters().get("session");
	}

	@Override
	public boolean isOpen() {
		return this.closed == null;
	}

	@Override
	public Basic getBasicRemote() {
		if (this.basic == null) {
			this.basic = createBasic();
		}
		return this.basic;
	}

	@Override
	public boolean isSecure() {
		return this.instigatingReq.isSecure();
	}

	@Override
	public URI getRequestURI() {
		return URI.create(this.instigatingReq.getRequestURI());
	}

	@Override
	public String getQueryString() {
		return this.instigatingReq.getQueryString();
	}

	@Override
	public Map<String, String> getPathParameters() {
		return this.pathParams;
	}

	@Override
	public Principal getUserPrincipal() {
		return this.instigatingReq.getUserPrincipal();
	}

	@Override
	public Map<String, List<String>> getRequestParameterMap() {
		final Map<String, List<String>> paramMap = new HashMap<>();
		this.instigatingReq.getParameterMap().forEach((k, v) -> paramMap.put(k, Arrays.asList(v)));
		return Collections.unmodifiableMap(paramMap);
	}

	@Override
	public void close() throws IOException {
		close(null);
	}

	@Override
	public void close(CloseReason closeReason) throws IOException {
		this.log.info("Closing session. [id={},reason={}]", getId(), closeReason);
		this.servlet.unregister(this);
		this.closed = this.active = LocalDateTime.now();
	}

	/**
	 * 
	 * @return
	 */
	public LocalDateTime activeTime() {
		return this.active;
	}

	/**
	 * @return the closed
	 */
	public LocalDateTime closedTime() {
		return closed;
	}

	/**
	 * 
	 * @param sender
	 * @throws IOException 
	 */
	boolean setSender(Sender sender) throws IOException {
		if (checkStillValid()) {

		}
		synchronized (this) {
			if (this.sender != null && sender != null) {
				return false; // Sender already set!
			}
			this.sender = sender;
		}
		if (sender != null) {
			flush();
		}
		return true;
	}

	/**
	 * @throws IOException 
	 */
	private void flush() throws IOException {
		checkStillValid();
		if (this.sender == null) {
			this.log.debug("No sender. Ignoring flush. [sessionId={}]", getId());
			return;
		}
		this.sender.send(this.frameQueue);
		this.active = LocalDateTime.now();
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	private boolean checkStillValid() throws IOException {
		if (!isOpen()) {
			return false;
		}
		if (this.active != null && this.active.plus(5, ChronoUnit.SECONDS).isBefore(LocalDateTime.now())) {
			close();
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		ServletSession other = (ServletSession) obj;
		return Objects.equals(getId(), other.getId());
	}


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [29 Jul 2016]
	 */
	public class DefaultBasic extends AbstractBasic {
		private final Logger log = LoggerFactory.getLogger(DefaultBasic.class);
		private final StringBuilder buf = new StringBuilder();

		@Override
		public void sendText(String msg) throws IOException {
			this.log.info("Sending message. [sessionId={},msg={}]", getId(), msg);
			frameQueue.add(msg);
			flush();
		}

		@Override
		public void sendText(String msg, boolean last) throws IOException {
			this.log.info("Sending message. [sessionId={},msg={},last={}]", getId(), msg, last);
			synchronized (this.buf) {
				this.buf.append(msg);
				if (last) {
					frameQueue.add(buf.toString());
					this.buf.setLength(0);
					this.buf.trimToSize();
					flush();
				}
			}
		}
	}
}
