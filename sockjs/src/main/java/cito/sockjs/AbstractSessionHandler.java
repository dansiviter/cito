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

/**
 * @author Daniel Siviter
 * @since v1.0 [23 Feb 2017]
 */
public abstract class AbstractSessionHandler extends AbstractHandler {
	private final boolean createSession;

	/**
	 * @param servlet
	 * @param methods
	 */
	public AbstractSessionHandler(Servlet servlet, String mediaType, boolean createSession, String... methods) {
		super(servlet, mediaType, methods);
		this.createSession = createSession;
	}

	/**
	 * 
	 * @param asyncCtx
	 * @throws ServletException
	 * @throws IOException
	 */
	public void handle(HttpAsyncContext async) throws ServletException, IOException {

		ServletSession session = this.servlet.getSession(async.getRequest());

		boolean initial = false;
		if (session == null) {
			initial = true;
			if (this.createSession) {
				if (this.log.isInfoEnabled()) {
					this.log.info("New session! [{}]", Util.session(this.servlet, async.getRequest()));
				}
				session = this.servlet.createSession(async.getRequest());
			}
		}

		handle(async, session, initial);
	}

	/**
	 * 
	 * @param async
	 * @param session
	 * @param initial
	 * @throws ServletException
	 * @throws IOException
	 */
	protected abstract void handle(HttpAsyncContext async, ServletSession session, boolean initial)
			throws ServletException, IOException;
}
