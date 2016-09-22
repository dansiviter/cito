package cito.stomp.server.security;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import cito.stomp.Command;
import cito.stomp.server.SecurityContext;
import cito.stomp.server.annotation.RolesAllowedLiteral;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [30 Aug 2016]
 */
public class SecurityRegistry {
	private Map<Matcher, Limitation> limitations = new LinkedHashMap<>();

	/**
	 * 
	 * @param patterns
	 * @return
	 */
	public Constraint matches(String... patterns) {
		return matches(null, patterns);
	}

	/**
	 * 
	 * @param command
	 * @param patterns
	 * @return
	 */
	public Constraint matches(Command command, String... patterns) {
		return new Constraint(command, patterns);
	}



	/// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [30 Aug 2016]
	 */
	private static class Matcher {

		public Matcher(Command command, String pattern) {
			// TODO Auto-generated constructor stub
		}

	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [30 Aug 2016]
	 */
	public class Constraint {
		private final Command command;
		private final String[] patterns;
		
		/**
		 * 
		 * @param command
		 * @param patterns
		 */
		public Constraint(Command command, String[] patterns) {
			this.command = command;
			this.patterns = patterns;
		}

		/**
		 * 
		 * @param roles
		 * @return
		 */
		public SecurityRegistry isInRole(String... roles) {
			return limit(new AnnotationSecurityLimitation(new RolesAllowedLiteral(roles)));
		}

		/**
		 * 
		 * @param limitation
		 * @return
		 */
		public SecurityRegistry limit(Limitation limitation) {
			for (String pattern : this.patterns) {
				limitations.put(new Matcher(this.command, pattern), limitation);
			}
			return SecurityRegistry.this;
		}
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [30 Aug 2016]
	 */
	@FunctionalInterface
	public interface Limitation {
		boolean isPermitted(SecurityContext securityCtx);	
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [30 Aug 2016]
	 */
	public class AnnotationSecurityLimitation implements Limitation {
		private final Annotation annotation;

		public AnnotationSecurityLimitation(Annotation annotation) {
			if (annotation.annotationType() != DenyAll.class ||
					annotation.annotationType() != DenyAll.class ||
					annotation.annotationType() != DenyAll.class)
			{
				throw new IllegalArgumentException("Not supported! [" + annotation + "]");
			}
			this.annotation = annotation;
		}

		@Override
		public boolean isPermitted(SecurityContext securityCtx) {
			if (this.annotation.annotationType() == DenyAll.class) {
				return false;
			}

			if (this.annotation.annotationType() == RolesAllowed.class) {
				final String[] roles = ((RolesAllowed) this.annotation).value();

				for (String role : roles) {
					if (securityCtx.isUserInRole(role)) {
						return true;
					}
				}
			}

			if (this.annotation.annotationType() == PermitAll.class) {
				return true;
			}

			return false;
		}
	}
}
