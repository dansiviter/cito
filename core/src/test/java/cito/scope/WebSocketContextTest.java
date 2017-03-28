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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.websocket.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import cito.QuietClosable;
import cito.ReflectionUtil;
import cito.SingletonProvider;
import cito.annotation.WebSocketScope;
import cito.server.SessionRegistry;

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
	@Mock
	private SessionRegistry sessionRegistry;

	private WebSocketContext context;

	@Before
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void before() {
		when(this.session.getId()).thenReturn("sessionId");

		final CreationalContext creationalContext = mock(CreationalContext.class);
		when(this.beanManager.createCreationalContext(null)).thenReturn(creationalContext);
		final AnnotatedType annotatedType = mock(AnnotatedType.class);
		when(this.beanManager.createAnnotatedType(WebSocketContext.class)).thenReturn(annotatedType);
		final InjectionTarget injectionTarget = mock(InjectionTarget.class);
		when(this.beanManager.createInjectionTarget(annotatedType)).thenReturn(injectionTarget);

		this.context = new WebSocketContext(this.beanManager);
		ReflectionUtil.set(this.context, "log", this.log);
		ReflectionUtil.set(this.context, "sessionRegistry", new SingletonProvider<SessionRegistry>(this.sessionRegistry));
		this.context.init(this.sessionHolder);
	
		verify(this.beanManager).createCreationalContext(null);
		verify(this.beanManager).createAnnotatedType(WebSocketContext.class);
		verify(this.beanManager).createInjectionTarget(annotatedType);
		verify(injectionTarget).inject(this.context, creationalContext);
		verifyNoMoreInteractions(creationalContext, annotatedType, injectionTarget);
	}

	@Test
	public void activate() {
		final QuietClosable closable = this.context.activate(session);
		closable.close();

		verify(this.session).getId();
		verify(this.sessionHolder).set(this.session);
		verify(this.sessionHolder).remove();
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
		verify(this.sessionHolder).remove();
		verify(this.log).debug("Activiating scope. [sessionId={}]", "sessionId");
		verify(this.log).debug("Disposing scope. [sessionId={}]", "sessionId");
	}

	@After
	public void after() {
		verify(this.beanManager).isPassivatingScope(WebSocketScope.class);
		verifyNoMoreInteractions(this.log, this.sessionRegistry, this.beanManager, this.sessionHolder, this.session);
	}
}
