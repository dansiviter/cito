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
import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedTransferQueue;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.MessageHandler;
import javax.websocket.MessageHandler.Partial;
import javax.websocket.MessageHandler.Whole;
import javax.websocket.RemoteEndpoint.Basic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [3 Jan 2017]
 */
public class ServletSession extends SessionAdapter {
	protected static final String FRAME_DELIMITER = "\n";

	private final Set<MessageHandlerWrapper> messageHandlers = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private final LinkedTransferQueue<String> frameQueue = new LinkedTransferQueue<>();

	private final Logger log;
	private final HttpServletRequest instigatingReq;
	private final Endpoint endpoint;
	private final Map<String, String> pathParams;

	private Basic basic;
	private boolean open = true;
	//	private LocalDateTime lastflush;
	private Sender sender;

	/**
	 * 
	 * @param servlet
	 * @param instigatingReq
	 * @throws ServletException
	 */
	public ServletSession(
			Servlet servlet,
			HttpServletRequest instigatingReq)
					throws ServletException
	{
		this.log = LoggerFactory.getLogger(getClass());
		this.instigatingReq = instigatingReq;
		this.endpoint = servlet.ctx.getConfig().createEndpoint();
		this.pathParams = Util.pathParams(servlet.ctx.getConfig(), instigatingReq);
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
		return this.open;
	}

	@Override
	public void addMessageHandler(MessageHandler handler) throws IllegalStateException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> void addMessageHandler(Class<T> clazz, Partial<T> handler) {
		this.messageHandlers.add(new MessageHandlerWrapper(handler, clazz));
	}

	@Override
	public <T> void addMessageHandler(Class<T> clazz, Whole<T> handler) {
		this.messageHandlers.add(new MessageHandlerWrapper(handler, clazz));
	}
	
	Set<MessageHandlerWrapper> getMessageHandlerWrappers() {
		return messageHandlers;
	}

	@Override
	public Set<MessageHandler> getMessageHandlers() {
		return getMessageHandlerWrappers().stream().map(MessageHandlerWrapper::handler).collect(Collectors.toSet());
	}

	@Override
	public void removeMessageHandler(MessageHandler handler) {
		this.messageHandlers.remove(handler);
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
	public void close() throws IOException {
		close(null);
	}

	@Override
	public void close(CloseReason closeReason) throws IOException {
		this.log.info("Closing... [{}]", closeReason);
		this.open = false;
	}

	/**
	 * 
	 * @param sender
	 * @throws IOException 
	 */
	synchronized void setSender(Sender sender) throws IOException {
		if (this.sender != null && sender != null) {
			throw new IllegalStateException("Sender already set!");
		}

		this.sender = sender;
		flush();
	}

	/**
	 * @throws IOException 
	 */
	private synchronized void flush() throws IOException {
		if (this.sender == null) {
			this.log.info("No sender. Ignoring flush. [sessionId={}]", getId());
			return;
		}
		String frame;
		while ((frame = this.frameQueue.poll()) != null) {
			this.log.info("Flushing frame. [sessionId={},frame={}]", getId(), frame);
			this.sender.send(frame, this.frameQueue.isEmpty());
		}
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
	 * @since v1.0 [14 Feb 2017]
	 */
	public static class MessageHandlerWrapper {
		public final MessageHandler handler;
		public final Class<?> clazz;

		private MessageHandlerWrapper(MessageHandler handler, Class<?> clazz) {
			this.handler = handler;
			this.clazz = clazz;
		}

		/**
		 * @return the handler
		 */
		public MessageHandler handler() {
			return handler;
		}
	}


	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [29 Jul 2016]
	 */
	public class DefaultBasic extends AbstractBasic {
		private StringBuilder buf = new StringBuilder();

		@Override
		public void sendText(String msg) throws IOException {
			log.info("Sending message. [sessionId={},msg={}]", getId(), msg);
			frameQueue.add(msg);
			flush();
		}

		@Override
		public void sendText(String msg, boolean last) throws IOException {
			log.info("Sending message. [sessionId={},msg={},last={}]", getId(), msg, last);
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
