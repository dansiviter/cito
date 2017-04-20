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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.websocket.Endpoint;
import javax.websocket.MessageHandler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for {@link ServletSession}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [20 Apr 2017]
 */
@RunWith(MockitoJUnitRunner.class)
public class ServletSessionTest {
	@Mock
	private Servlet servlet;
	@Mock
	private HttpServletRequest instigatingReq;
	@Mock
	private Config config;
	@Mock
	private Endpoint endpoint;

	private ServletSession servletSession;

	@Before
	public void before() throws ServletException {
		when(this.servlet.getConfig()).thenReturn(this.config);
		when(this.config.createEndpoint()).thenReturn(this.endpoint);
		when(this.instigatingReq.getRequestURI()).thenReturn("/context/xxx/xxx/blagh");
		when(this.config.path()).thenReturn("context");

		this.servletSession = new ServletSession(this.servlet, this.instigatingReq);
	}

	@Test
	public void messageHandlers() {
		final MessageHandler handler = mock(MessageHandler.class);

		this.servletSession.addMessageHandler(handler);

		assertTrue(this.servletSession.getMessageHandlers().contains(handler));

		this.servletSession.removeMessageHandler(handler);

		assertFalse(this.servletSession.getMessageHandlers().contains(handler));

		verifyNoMoreInteractions(handler);
	}

	@After
	public void after() throws ServletException {
		verify(this.servlet, times(2)).getConfig();
		verify(this.config).createEndpoint();
		verify(this.instigatingReq, times(2)).getRequestURI();
		verify(this.config, times(2)).path();
		verifyNoMoreInteractions(this.servlet, this.instigatingReq);
	}
}
