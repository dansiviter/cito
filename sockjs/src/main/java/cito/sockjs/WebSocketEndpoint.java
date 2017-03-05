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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

/**
 * @author Daniel Siviter
 * @since v1.0 [24 Feb 2017]
 */
public class WebSocketEndpoint extends Endpoint {
	private ServletContext servletCtx;
	private Servlet servlet;
	private Endpoint delegate;

	@Override
	public void onOpen(Session session, EndpointConfig endpointConfig) {
		this.servletCtx = (ServletContext) endpointConfig.getUserProperties().get(ServletContext.class.getSimpleName());
		this.servlet = (Servlet) this.servletCtx.getAttribute(Servlet.class.getName());
		try {
			this.delegate = this.servlet.getConfig().createEndpoint();
		} catch (ServletException e) {
			this.servletCtx.log("Unable to create delegate!", e);
			try {
				session.close();
			} catch (IOException e1) {
				this.servletCtx.log("Unable to close session!", e);
			}
			return;
		}
		this.delegate.onOpen(session, endpointConfig);
	}

	@Override
	public void onError(Session session, Throwable thr) {
		this.delegate.onError(session, thr);
	}

	@Override
	public void onClose(Session session, CloseReason closeReason) {
		this.delegate.onClose(session, closeReason);
	}
}
