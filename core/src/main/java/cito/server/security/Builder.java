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

import static cito.Util.requireNonEmpty;
import static java.util.Collections.singletonMap;
import static java.util.Collections.unmodifiableList;
import static org.apache.deltaspike.core.util.metadata.AnnotationInstanceProvider.of;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import cito.Glob;
import cito.server.SecurityContext;
import cito.stomp.Command;
import cito.stomp.Frame;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [19 Oct 2016]
 */
public class Builder {
	static final PermitAll PERMIT_ALL = of(PermitAll.class);
	static final DenyAll DENY_ALL = of(DenyAll.class);

	private final SecurityRegistry registry;
	private final List<FrameMatcher> frameMatchers = new ArrayList<>();
	private final List<SecurityMatcher> securityMatchers = new ArrayList<>();

	Builder(SecurityRegistry registry) {
		this.registry = registry;
	}

	/**
	 * 
	 * @param patterns
	 * @return
	 */
	public Builder matches(FrameMatcher matcher) {
		this.frameMatchers.add(matcher);
		return this;
	}

	/**
	 * 
	 * @param patterns
	 * @return
	 */
	public Builder matches(Command... commands) {
		return matches(new CommandMatcher(commands));
	}

	/**
	 * 
	 * @param patterns
	 * @return
	 */
	public Builder matches(String... destinations) {
		return matches(new DestinationsMatcher(destinations));
	}

	/**
	 * 
	 * @param constraint
	 * @return
	 */
	public Builder matches(SecurityMatcher matcher) {
		this.securityMatchers.add(matcher);
		return this;
	}

	/**
	 * 
	 * @return
	 */
	public Builder nullDestination() {
		return matches(new NullDestinationMatcher());
	}

	/**
	 * 
	 * @param roles
	 * @return
	 */
	public Builder roles(String... roles) {
		return matches(new SecurityAnnotationMatcher(of(RolesAllowed.class, singletonMap("value", roles))));
	}

	/**
	 * 
	 * @return
	 */
	public Builder permitAll() {
		return matches(new SecurityAnnotationMatcher(PERMIT_ALL));
	}

	/**
	 * 
	 * @return
	 */
	public Builder denyAll() {
		return matches(new SecurityAnnotationMatcher(DENY_ALL));
	}

	/**
	 * 
	 * @return
	 */
	public Builder principleExists() {
		return matches(new PrincipalMatcher());
	}

	/**
	 * 
	 * @return
	 */
	public Limitation build() {
		final Limitation limitation = new Limitation(this.frameMatchers, this.securityMatchers);
		this.registry.register(limitation);
		return limitation;
	}


	// --- Static Methods ---

	/**
	 * 
	 * @param roles
	 * @return
	 */
	public static RolesAllowed createRolesAllowed(String... roles) {
		return of(RolesAllowed.class, singletonMap("value", roles));
	}


	// --- Inner Classes ---

	/**
	 * A matcher for {@link Command}s.
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [18 Oct 2016]
	 */
	public static class CommandMatcher implements FrameMatcher {
		private final Set<Command> commands;

		public CommandMatcher(Command... commands) {
			this.commands = EnumSet.copyOf(Arrays.asList(commands));
		}

		@Override
		public boolean matches(Frame frame) {
			return this.commands.contains(frame.command());
		}
	}

	/**
	 * Simple interface to define a destination {@link FrameMatcher} type.
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [18 Oct 2016]
	 */
	public static abstract class DestinationMatcher implements FrameMatcher {
		@Override
		public final boolean matches(Frame frame) {
			return matches(frame.destination().orElse(null));
		}

		protected abstract boolean matches(String destination);
	}

	/**
	 * A {@link DestinationMatcher} that uses the exact destination.
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [18 Oct 2016]
	 */
	public static class ExactDestinationMatcher extends DestinationMatcher {
		private final String destination;

		public ExactDestinationMatcher(String destination) {
			this.destination = destination;	
		}

		@Override
		public boolean matches(String destination) {
			return Objects.equals(destination, this.destination);
		}
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [27 Oct 2016]
	 */
	public static class NullDestinationMatcher extends ExactDestinationMatcher {
		public NullDestinationMatcher() {
			super(null);
		}
	}

	/**
	 * A {@link DestinationMatcher} that uses {@link Glob} patterns.
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [18 Oct 2016]
	 */
	public static class GlobDestinationMatcher extends DestinationMatcher {
		private final Glob glob;

		public GlobDestinationMatcher(String destination) {
			this.glob = Glob.from(destination);
			if (!this.glob.hasWildcard()) {
				throw new IllegalArgumentException("Destination no a Glob pattern! [" + destination + "]");
			}
		}

		@Override
		public boolean matches(String destination) {
			return destination != null && this.glob.matches(destination);
		}
	}

	/**
	 * Defines a group of destinations to test. If this is a {@link Glob} type then {@link GlobDestinationMatcher},
	 * otherwise the simpler {@link ExactDestinationMatcher} is used for performance.
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [18 Oct 2016]
	 */
	public static class DestinationsMatcher implements FrameMatcher {
		private final DestinationMatcher[] matchers;

		public DestinationsMatcher(String... destinations) {
			if (destinations.length == 0) {
				throw new IllegalArgumentException("Must have one destination!");
			}
			this.matchers = new DestinationMatcher[destinations.length];

			for (int i = 0; i < destinations.length; i++) {
				if (destinations[i].contains("*")) {
					this.matchers[i] = new GlobDestinationMatcher(destinations[i]);
				} else {
					this.matchers[i] = new ExactDestinationMatcher(destinations[i]);
				}
			}
		}

		@Override
		public boolean matches(Frame frame) {
			for (DestinationMatcher matcher : this.matchers) {
				if (!matcher.matches(frame)) {
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [5 Oct 2016]
	 */
	public static class PrincipalMatcher implements SecurityMatcher {
		@Override
		public boolean permitted(SecurityContext securityCtx) {
			return securityCtx.getUserPrincipal() != null;
		}
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [30 Aug 2016]
	 */
	public static class SecurityAnnotationMatcher implements SecurityMatcher {
		private final Annotation annotation;

		public SecurityAnnotationMatcher(Annotation annotation) {
			if (annotation.annotationType() != PermitAll.class &&
					annotation.annotationType() != DenyAll.class &&
					annotation.annotationType() != RolesAllowed.class)
			{
				throw new IllegalArgumentException("Not supported! [" + annotation + "]");
			}
			this.annotation = annotation;
		}

		@Override
		public boolean permitted(SecurityContext securityCtx) {
			if (this.annotation.annotationType() == DenyAll.class) {
				return false;
			}

			if (this.annotation.annotationType() == PermitAll.class) { // XXX needed?
				return true;
			}

			if (this.annotation.annotationType() == RolesAllowed.class) {
				final String[] roles = ((RolesAllowed) this.annotation).value();

				for (String role : roles) {
					if (securityCtx.isUserInRole(role)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [25 Oct 2016]
	 */
	public static class Limitation implements FrameMatcher, SecurityMatcher {
		private static final AtomicInteger ID = new AtomicInteger();

		private final int id;
		private final List<FrameMatcher> frameMatchers;
		private final List<SecurityMatcher> securityMatchers;

		public Limitation(List<FrameMatcher> frameMatchers, List<SecurityMatcher> securityMatchers) {
			this.id = ID.incrementAndGet();
			this.frameMatchers = unmodifiableList(new ArrayList<>(requireNonEmpty(frameMatchers)));
			this.securityMatchers = unmodifiableList(new ArrayList<>(requireNonEmpty(securityMatchers)));
		}

		public int getId() {
			return id;
		}

		@Override
		public boolean matches(Frame frame) {
			for (FrameMatcher matcher : this.frameMatchers) {
				if (!matcher.matches(frame)) {
					return false;		
				}
			}
			return true;
		}

		@Override
		public boolean permitted(SecurityContext ctx) {
			for (SecurityMatcher matcher : this.securityMatchers) {
				if (!matcher.permitted(ctx)) {
					return false;
				}
			}
			return true;
		}
	}
}
