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
package cito.broker;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

import cito.QuietClosable;
import cito.event.DestinationChanged;

/**
 * Acts as both a interface to detect if {@link DestinationChanged}s will be produced by the broker and to hold/produce
 * {@link DestinationChanged} within the CDI runtime.
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Jan 2017]
 */
@ApplicationScoped
public class DestinationEventProducer {
	public static final ThreadLocal<DestinationChanged> HOLDER = new ThreadLocal<>();

	@Produces @Dependent
	public static DestinationChanged get() {
		return HOLDER.get();
	}

	/**
	 * 
	 * @param e
	 */
	public static QuietClosable set(DestinationChanged e) {
		final DestinationChanged old = get();
		if (old != null) {
			throw new IllegalStateException("Already set!");
		}
		HOLDER.set(e);
		return new QuietClosable() {
			@Override
			public void close() {
				HOLDER.remove();
			}
		};
	}
}