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

import static cito.ReflectionUtil.getAnnotationValue;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import cito.server.SecurityContext;
import cito.server.security.Builder.Limitation;
import cito.stomp.Frame;

/**
 * Registry should always allow {@code null} destinations.
 * 
 * @author Daniel Siviter
 * @since v1.0 [30 Aug 2016]
 */
@ApplicationScoped
public class SecurityRegistry {
	private final Set<Limitation> limitations = new LinkedHashSet<>();

	@Inject @Any
	private Instance<SecurityCustomiser> customisers;

	/**
	 * 
	 */
	@PostConstruct
	public void init() {
		final Set<SecurityCustomiser> configurers = new TreeSet<>(Comparator.comparing(SecurityRegistry::getPriority));
		this.customisers.forEach(configurers::add);
		configurers.forEach(c -> { 
			customise(c);
			this.customisers.destroy(c);
		});
	}

	/**
	 * 
	 * @param limitation
	 */
	public synchronized void register(Limitation limitation) {
		this.limitations.add(limitation);
	}

	/**
	 * 
	 * @param frame
	 * @return
	 */
	public synchronized List<Limitation> getMatching(Frame frame) {
		return this.limitations.stream().filter(e -> e.matches(frame)).collect(Collectors.toList());
	}

	/**
	 * 
	 * @param frame
	 * @param ctx
	 * @return
	 */
	public boolean isPermitted(Frame frame, SecurityContext ctx) {
		for (Limitation limitation : getMatching(frame)) {
			if (!limitation.permitted(ctx)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @return
	 */
	public Builder builder() {
		return builder(this);
	}

	/**
	 * 
	 * @param customiser
	 */
	public void customise(SecurityCustomiser customiser) {
		customiser.customise(this);
	}


	// --- Static Methods ---

	/**
	 * 
	 * @return
	 */
	public static Builder builder(SecurityRegistry registry) {
		return new Builder(registry);
	}

	/**
	 * Returns the {@link Priority#value()} if available or 5000 if not.
	 * 
	 * @param config
	 * @return
	 */
	private static int getPriority(SecurityCustomiser config) {
		return getAnnotationValue(config, Priority.class, 5000);
	}
}
