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

import javax.servlet.ServletException;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Siviter
 * @since v1.0 [24 Feb 2017]
 */
public class WebSocketEndpoint extends Endpoint {
	private static final Logger LOG = LoggerFactory.getLogger(WebSocketEndpoint.class);

	private Servlet servlet;
	private Endpoint delegate;
	private WebSocketSession session;

	@Override
	public void onOpen(Session session, EndpointConfig endpointConfig) {
		LOG.info("Opening session. [id={}]", session.getId());
		final WebSocketConfigurer configurer = (WebSocketConfigurer) ((ServerEndpointConfig) endpointConfig).getConfigurator();
		this.servlet = configurer.getServlet();

		try {
			this.delegate = this.servlet.getConfig().createEndpoint();
			LOG.info("Created delegate endpoint. [id={},delegate={}]", session.getId(), this.delegate);
		} catch (ServletException e) {
			LOG.error("Unable to create delegate!", e);
			try {
				session.close();
			} catch (IOException e1) {
				LOG.warn("Unable to close session!", e);
			}
			return;
		}
		this.session = new WebSocketSession(session);
		this.delegate.onOpen(this.session.sendOpen(), endpointConfig);
	}

	@Override
	public void onError(Session session, Throwable thr) {
		LOG.error("Error. [id={}]", session.getId(), thr);
		this.delegate.onError(this.session, thr);
	}

	@Override
	public void onClose(Session session, CloseReason closeReason) {
		LOG.error("Closing. [id={},reason={}]", session.getId(), closeReason);
		this.delegate.onClose(this.session, closeReason);
	}
}
