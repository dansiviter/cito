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
package cito.server.security;

import static cito.ReflectionUtil.getAnnotation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import cito.ReflectionUtil;
import cito.server.SecurityContext;
import cito.server.security.Builder.Limitation;
import cito.stomp.Frame;

/**
 * Unit tests for {@link Builder}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [14 Apr 2017]
 */
@RunWith(MockitoJUnitRunner.class)
public class SecurityRegistryTest {
	@Mock
	private Instance<SecurityCustomiser> customisers;
	@Mock
	private SecurityCustomiser customiser;
	@InjectMocks
	private SecurityRegistry registry;

	@Test
	public void scope() {
		assertNotNull(getAnnotation(SecurityRegistry.class, ApplicationScoped.class));
	}

	@Test
	public void init() {
		this.registry.init();

		verify(this.customisers).forEach(any());
	}

	@Test
	public void register() {
		final Limitation limitation = mock(Limitation.class);

		this.registry.register(limitation);

		assertEquals(1, getLimitations().size());
		assertTrue(getLimitations().contains(limitation));

		verifyNoMoreInteractions(limitation);
	}

	@Test
	public void getMatching() {
		final Frame frame = mock(Frame.class);
		final Limitation limitation = mock(Limitation.class);
		getLimitations().add(limitation);
		when(limitation.matches(frame)).thenReturn(true);
		final Limitation limitation0 = mock(Limitation.class);
		getLimitations().add(limitation0);

		this.registry.getMatching(frame);

		verify(limitation).matches(frame);
		verify(limitation0).matches(frame);
		verifyNoMoreInteractions(limitation, limitation0, frame);
	}

	@Test
	public void isPermitted() {
		final Frame frame = mock(Frame.class);
		final SecurityContext context = mock(SecurityContext.class);
		final Limitation limitation = mock(Limitation.class);
		getLimitations().add(limitation);
		when(limitation.matches(frame)).thenReturn(true);
		when(limitation.permitted(context)).thenReturn(true);

		assertTrue(this.registry.isPermitted(frame, context));

		verify(limitation).matches(frame);
		verify(limitation).permitted(context);
		verifyNoMoreInteractions(frame, context, limitation);
	}

	@Test
	public void isPermitted_false() {
		final Frame frame = mock(Frame.class);
		final SecurityContext context = mock(SecurityContext.class);
		final Limitation limitation = mock(Limitation.class);
		getLimitations().add(limitation);
		when(limitation.matches(frame)).thenReturn(true);

		assertFalse(this.registry.isPermitted(frame, context));

		verify(limitation).matches(frame);
		verify(limitation).permitted(context);
		verifyNoMoreInteractions(frame, context, limitation);
	}

	@Test
	public void builder() {
		final Builder builder = this.registry.builder();
		final Builder builder0 = this.registry.builder();

		Assert.assertNotSame(builder, builder0);
	}

	@Test
	public void configure() {
		this.registry.customise(customiser);

		verify(customiser).customise(this.registry);
	}

	@After
	public void after() {
		verifyNoMoreInteractions(this.customisers, this.customiser);
	}

	/**
	 * 
	 * @return
	 */
	private Set<Limitation> getLimitations() {
		return ReflectionUtil.get(this.registry, "limitations");
	}
}
