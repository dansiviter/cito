package cito.stomp.server.security;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import cito.stomp.Command;
import cito.stomp.Frame;
import cito.stomp.Glob;
import cito.stomp.Headers;
import cito.stomp.server.SecurityContext;
import cito.stomp.server.annotation.DenyAllLiteral;
import cito.stomp.server.annotation.PermitAllLiteral;
import cito.stomp.server.annotation.RolesAllowedLiteral;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [19 Oct 2016]
 */
public class Builder {

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
	 * @return
	 */
	public Builder nullDestination() {
		return matches(new NullDestinationMatcher());
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
	 * @param roles
	 * @return
	 */
	public Builder roles(String... roles) {
		return matches(new SecurityAnnotationMatcher(new RolesAllowedLiteral(roles)));
	}

	/**
	 * 
	 * @return
	 */
	public Builder permitAll() {
		return matches(new SecurityAnnotationMatcher(new PermitAllLiteral()));
	}

	/**
	 * 
	 * @return
	 */
	public Builder denyAll() {
		return matches(new SecurityAnnotationMatcher(new DenyAllLiteral()));
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


	// --- Inner Classes ---

	/**
	 * A matcher for {@link Command}s.
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [18 Oct 2016]
	 */
	private static class CommandMatcher implements FrameMatcher {
		private final Set<Command> commands;

		public CommandMatcher(Command... commands) {
			this.commands = EnumSet.copyOf(Arrays.asList(commands));
		}

		@Override
		public boolean matches(Frame frame) {
			return this.commands.contains(frame.getCommand());
		}
	}

	/**
	 * Simple interface to define a destination {@link FrameMatcher} type.
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [18 Oct 2016]
	 */
	private static abstract class DestinationMatcher implements FrameMatcher {
		@Override
		public final boolean matches(Frame frame) {
			return matches(frame.containsHeader(Headers.DESTINATION) ? frame.destination() : null);
		}

		protected abstract boolean matches(String destination);
	}

	/**
	 * A {@link DestinationMatcher} that uses the exact destination.
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [18 Oct 2016]
	 */
	private static class ExactDestinationMatcher extends DestinationMatcher {
		private final String destination;

		public ExactDestinationMatcher(String destination) {
			this.destination = destination;	
		}

		@Override
		public boolean matches(String destination) {
			return destination != null && this.destination.equals(destination);
		}
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [27 Oct 2016]
	 */
	private static class NullDestinationMatcher extends ExactDestinationMatcher {
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
	private static class GlobDestinationMatcher extends DestinationMatcher {
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
	private static class DestinationsMatcher implements FrameMatcher {
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
		public void isPermitted(SecurityContext securityCtx) throws SecurityViolationException{
			if (securityCtx.getUserPrincipal() == null) {
				throw new SecurityViolationException("No user principal!");
			}
		}
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [30 Aug 2016]
	 */
	public class SecurityAnnotationMatcher implements SecurityMatcher {
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
		public void isPermitted(SecurityContext securityCtx) throws SecurityViolationException {
			if (this.annotation.annotationType() == DenyAll.class) {
				throw new SecurityViolationException("All denied!");
			}

			if (this.annotation.annotationType() == PermitAll.class) { // XXX needed?
				return;
			}

			if (this.annotation.annotationType() == RolesAllowed.class) {
				final String[] roles = ((RolesAllowed) this.annotation).value();

				for (String role : roles) {
					if (securityCtx.isUserInRole(role)) {
						return;
					}
				}
				throw new SecurityViolationException("Not in roles! " + Arrays.toString(roles));
			}


		}
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [25 Oct 2016]
	 */
	public static class Limitation implements FrameMatcher, SecurityMatcher {
		private final List<FrameMatcher> frameMatchers;
		private final List<SecurityMatcher> securityMatchers;

		public Limitation(List<FrameMatcher> frameMatchers, List<SecurityMatcher> securityMatchers) {
			this.frameMatchers = Collections.unmodifiableList(new ArrayList<>(frameMatchers));
			this.securityMatchers = Collections.unmodifiableList(new ArrayList<>(securityMatchers));
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
		public void isPermitted(SecurityContext ctx) throws SecurityViolationException {
			for (SecurityMatcher matcher : this.securityMatchers) {
				matcher.isPermitted(ctx);		
			}
		}
	}
}
