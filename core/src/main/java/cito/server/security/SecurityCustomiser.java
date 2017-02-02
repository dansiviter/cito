package cito.server.security;

import javax.enterprise.context.Dependent;

/**
 * Used for configuring the the {@link SecurityRegistry}. It's recommended these are created using {@link Dependent} as
 * they'll only be used on startup of the {@link SecurityRegistry} and then discarded.
 * 
 * <pre>
 *	&#064;Dependent
 *	public class Configurer implements SecurityCustomiser {
 *		&#064;Override
 *		public void configure(SecurityRegistry registry) {
 *			registry.builder().nullDestination().permitAll(); // important for most message types including CONNECT, DISCONNECT
 *			registry.builder().matches("/topic/rate.*").principleExists().build(); // user must be logged in
 *			registry.builder().matches("/topic/rate.EURUSD").roles("trader", "sales").build(); // user has roles 'trader' OR 'sales', logged in is implied
 *		}
 *	}
 * </pre>
 * 
 * It is possible to perform registration on the fly using the registry directly:
 * <pre>
 * 	&#064;Inject
 * 	private SecurityRegistry registry;
 * 
 * 	public void add() {
 * 		registry.configure(r -> r.builder().nullDestination().permitAll());
 * 	}
 * </pre>
 * 
 * @author Daniel Siviter
 * @since v1.0 [5 Oct 2016]
 */
@FunctionalInterface
public interface SecurityCustomiser {
	/**
	 * 
	 * @param registry
	 */
	void configure(SecurityRegistry registry);
}
