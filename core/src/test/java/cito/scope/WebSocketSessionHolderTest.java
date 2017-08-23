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

import static cito.ReflectionUtil.getAnnotation;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.Session;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import cito.QuietClosable;

/**
 * @author Daniel Siviter
 * @since v1.0 [17 Apr 2017]
 */
@RunWith(MockitoJUnitRunner.class)
public class WebSocketSessionHolderTest {
	@Mock
	public Logger log;

	@InjectMocks
	public WebSocketSessionHolder holder;

	@Test
	public void scope() {
		assertNotNull(getAnnotation(WebSocketSessionHolder.class, ApplicationScoped.class));
	}

	@Test
	public void set() {
		final Session session = Mockito.mock(Session.class);
		when(session.getId()).thenReturn("sessionId");

		assertNull(this.holder.get());
		try (QuietClosable closable = this.holder.set(session)) {
			assertNotNull(this.holder.get());
		}
		assertNull(this.holder.get());

		verify(session).getId();
		verify(this.log).debug("Setting session. [sessionId={}]", "sessionId");
		verifyNoMoreInteractions(session);
	}
}
