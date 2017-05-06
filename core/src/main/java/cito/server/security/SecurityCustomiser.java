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
 *			registry.builder().matches("topic/rate.*").principleExists().build(); // user must be logged in
 *			registry.builder().matches("topic/rate.EURUSD").roles("trader", "sales").build(); // user has roles 'trader' OR 'sales', logged in is implied
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
	void customise(SecurityRegistry registry);
}
