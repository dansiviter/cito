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
package cito.scope;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.spi.BeanManager;
import javax.websocket.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import cito.QuietClosable;
import cito.annotation.WebSocketScope;
import cito.scope.WebSocketContext;
import cito.scope.WebSocketSessionHolder;

/**
 * Unit test for {@link WebSocketContext}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [22 Nov 2016]
 */
@RunWith(MockitoJUnitRunner.class)
@Ignore
public class WebSocketContextTest {
	@Mock
	private BeanManager beanManager;
	@Mock
	private WebSocketSessionHolder sessionHolder;
	@Mock
	private Session session;

	private WebSocketContext context;

	@Before
	public void before() {
		when(this.session.getId()).thenReturn("sessionId");

		this.context = new WebSocketContext(this.beanManager);
		this.context.init(this.sessionHolder);
	}

	@Test
	public void activate() {
		final QuietClosable closable = this.context.activate(session);
		closable.close();

		verify(this.session).getId();
		verify(this.sessionHolder).set(this.session);
		verify(this.sessionHolder).remove();
	}

	@Test
	public void dispose() {
		this.context.dispose(session);

		verify(this.session).getId();
		verify(this.sessionHolder).set(this.session);
		verify(this.sessionHolder).remove();
	}

	@After
	public void after() {
		verify(beanManager).isPassivatingScope(WebSocketScope.class);
		verifyNoMoreInteractions(this.beanManager, this.sessionHolder, this.session);
	}
}