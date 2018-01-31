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

import static cito.server.security.Builder.DENY_ALL;
import static cito.server.security.Builder.PERMIT_ALL;
import static cito.server.security.Builder.createRolesAllowed;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.List;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import cito.ReflectionUtil;
import cito.server.SecurityContext;
import cito.server.security.Builder.NullDestinationMatcher;
import cito.server.security.Builder.PrincipalMatcher;
import cito.server.security.Builder.SecurityAnnotationMatcher;
import cito.stomp.Command;

/**
 * Unit tests for {@link Builder}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [14 Apr 2017]
 */
public class BuilderTest {
	@Rule
	public MockitoRule mockito = MockitoJUnit.rule();

	@Mock
	private SecurityRegistry registry;
	@Mock
	private SecurityContext context;

	private Builder builder;

	@Before
	public void before() {
		this.builder = new Builder(this.registry);
	}

	@Test
	public void matches_frameMatcher() {
		final FrameMatcher frameMatcher = mock(FrameMatcher.class);

		this.builder.matches(frameMatcher);

		verifyNoMoreInteractions(frameMatcher);
	}

	@Test
	public void matches_commands() {
		this.builder.matches(Command.COMMIT, Command.MESSAGE);
	}

	@Test
	public void matches_destinations() {
		this.builder.matches("/here", "/there");
	}

	@Test
	public void matches_securityMatcher() {
		final SecurityMatcher securityMatcher = mock(SecurityMatcher.class);

		this.builder.matches(securityMatcher);

		verifyNoMoreInteractions(securityMatcher);
	}

	@Test
	public void nullDestination() {
		this.builder.nullDestination();

		assertTrue(getSecurityMatchers().isEmpty());
		assertEquals(1, getFrameMatchers().size());
		assertEquals(NullDestinationMatcher.class, getFrameMatchers().get(0).getClass());
	}

	@Test
	public void roles() {
		this.builder.roles("this", "that");

		assertTrue(getFrameMatchers().isEmpty());
		assertEquals(1, getSecurityMatchers().size());
		SecurityAnnotationMatcher matcher = (SecurityAnnotationMatcher) getSecurityMatchers().get(0);
		assertTrue(RolesAllowed.class.isAssignableFrom(ReflectionUtil.get(matcher, "annotation").getClass()));
	}

	@Test
	public void permitAll() {
		this.builder.permitAll();

		assertTrue(getFrameMatchers().isEmpty());
		assertEquals(1, getSecurityMatchers().size());
		final SecurityAnnotationMatcher matcher = (SecurityAnnotationMatcher) getSecurityMatchers().get(0);
		assertTrue(PermitAll.class.isAssignableFrom(ReflectionUtil.get(matcher, "annotation").getClass()));
	}

	@Test
	public void denyAll() {
		this.builder.denyAll();

		assertTrue(getFrameMatchers().isEmpty());
		assertEquals(1, getSecurityMatchers().size());
		SecurityAnnotationMatcher matcher = (SecurityAnnotationMatcher) getSecurityMatchers().get(0);
		assertTrue(DenyAll.class.isAssignableFrom(ReflectionUtil.get(matcher, "annotation").getClass()));
	}

	@Test
	public void principleExists() {
		this.builder.principleExists();

		assertTrue(getFrameMatchers().isEmpty());
		assertEquals(1, getSecurityMatchers().size());
		assertEquals(PrincipalMatcher.class, getSecurityMatchers().get(0).getClass());
	}

	@Test
	public void build() {
		final FrameMatcher frameMatcher = mock(FrameMatcher.class);
		getFrameMatchers().add(frameMatcher);
		final SecurityMatcher securityMatcher = mock(SecurityMatcher.class);
		getSecurityMatchers().add(securityMatcher);

		this.builder.build();

		verify(this.registry).register(any());
		verifyNoMoreInteractions(frameMatcher, securityMatcher);
	}

	@Test
	public void principalMatcher() {
		final Principal principal = mock(Principal.class);
		when(this.context.getUserPrincipal()).thenReturn(principal);
		final PrincipalMatcher matcher = new PrincipalMatcher();

		assertTrue(matcher.permitted(this.context));

		verify(this.context).getUserPrincipal();
		verifyNoMoreInteractions(principal);
	}

	@Test
	public void principalMatcher_false() {
		final PrincipalMatcher matcher = new PrincipalMatcher();

		assertFalse(matcher.permitted(this.context));

		verify(this.context).getUserPrincipal();
	}

	@Test
	public void securityAnnotationMatcher_rolesAllowed() {
		when(this.context.isUserInRole("that")).thenReturn(true);
		final SecurityAnnotationMatcher matcher = new SecurityAnnotationMatcher(createRolesAllowed("this", "that"));

		assertTrue(matcher.permitted(this.context));

		verify(this.context).isUserInRole("this");
		verify(this.context).isUserInRole("that");
	}

	@Test
	public void securityAnnotationMatcher_rolesAllowed_false() {
		final SecurityAnnotationMatcher matcher = new SecurityAnnotationMatcher(createRolesAllowed("this", "that"));

		assertFalse(matcher.permitted(this.context));

		verify(this.context).isUserInRole("this");
		verify(this.context).isUserInRole("that");
	}

	@Test
	public void securityAnnotationMatcher_permitAll() {
		final SecurityAnnotationMatcher matcher = new SecurityAnnotationMatcher(PERMIT_ALL);

		assertTrue(matcher.permitted(this.context));
	}

	@Test
	public void securityAnnotationMatcher_denyAll() {
		final SecurityAnnotationMatcher matcher = new SecurityAnnotationMatcher(DENY_ALL);

		assertFalse(matcher.permitted(this.context));
	}

	@Test
	public void nullDestinationMatcher() {
		final NullDestinationMatcher matcher = new NullDestinationMatcher();

		assertTrue(matcher.matches((String) null));
	}

	@Test
	public void nullDestinationMatcher_false() {
		final NullDestinationMatcher matcher = new NullDestinationMatcher();

		assertFalse(matcher.matches("/"));
	}

	@After
	public void after() {
		verifyNoMoreInteractions(this.registry, this.context);
	}

	/**
	 * 
	 * @return
	 */
	private List<FrameMatcher> getFrameMatchers() {
		return ReflectionUtil.get(this.builder, "frameMatchers");
	}

	/**
	 * 
	 * @return
	 */
	private List<SecurityMatcher> getSecurityMatchers() {
		return ReflectionUtil.get(this.builder, "securityMatchers");
	}
}
