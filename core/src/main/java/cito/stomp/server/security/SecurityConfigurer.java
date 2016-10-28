package cito.stomp.server.security;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;

/**
 * Used for configuring the the {@link SecurityRegistry}. It's recommended these are created using {@link Dependent} as
 * they'll only be used on startup of the {@link SecurityRegistry} and then discarded.
 * 
 * TODO
 * 
 * @author Daniel Siviter
 * @since v1.0 [5 Oct 2016]
 */
@Dependent
public interface SecurityConfigurer {
	/**
	 * 
	 * @param registry
	 */
	void configure(SecurityRegistry registry);

	@Dependent @Priority(1)
	public static class Blagh implements SecurityConfigurer {
		@Override
		public void configure(SecurityRegistry registry) {
			registry.builder().nullDestination().permitAll();
//			registry.builder().matches("/*").principleExists().build();
		}
	}

	@Dependent @Priority(2)
	public static class Blagh1 implements SecurityConfigurer {
		@Override
		public void configure(SecurityRegistry registry) {
		}
	}

	@Dependent @Priority(3)
	public static class Blagh2 implements SecurityConfigurer {
		@Override
		public void configure(SecurityRegistry registry) {
		}
	}
}
