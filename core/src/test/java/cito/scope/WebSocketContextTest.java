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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.spi.BeanManager;
import javax.websocket.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import cito.QuietClosable;
import cito.ReflectionUtil;
import cito.annotation.WebSocketScope;

/**
 * Unit test for {@link WebSocketContext}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [22 Nov 2016]
 */
@RunWith(MockitoJUnitRunner.class)
public class WebSocketContextTest {
	@Mock
	private Logger log;
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
		ReflectionUtil.set(this.context, "log", this.log);
		this.context.init(this.sessionHolder);
	}

	@Test
	public void activate() {
		final QuietClosable closable = () -> { };
		when(this.sessionHolder.set(this.session)).thenReturn(closable);

		final QuietClosable actual = this.context.activate(session);
		assertEquals(closable, actual);

		verify(this.session).getId();
		verify(this.sessionHolder).set(this.session);
		verify(this.log).debug("Activiating scope. [sessionId={}]", "sessionId");
	}

	@Test
	public void activate_alreadyActive() {
		doThrow(IllegalArgumentException.class).when(this.sessionHolder).set(any(Session.class));

		IllegalArgumentException expected = null;
		try {
			this.context.activate(this.session);
		} catch (IllegalArgumentException e) {
			expected = e;
		}
		assertNotNull(expected);

		verify(this.session).getId();
		verify(this.sessionHolder).set(this.session);
		verify(this.log).debug("Activiating scope. [sessionId={}]", "sessionId");
	}

	@Test
	public void dispose() {
		when(this.sessionHolder.get()).thenReturn(this.session);

		this.context.dispose(this.session);

		verify(this.session, times(3)).getId();
		verify(this.sessionHolder).set(this.session);
		verify(this.sessionHolder).get();
		verify(this.log).debug("Activiating scope. [sessionId={}]", "sessionId");
		verify(this.log).debug("Disposing scope. [sessionId={}]", "sessionId");
	}

	@After
	public void after() {
		verify(this.beanManager).isPassivatingScope(WebSocketScope.class);
		verifyNoMoreInteractions(this.log, this.beanManager, this.sessionHolder, this.session);
	}
}
