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
package cito;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.slf4j.Logger;

/**
 * XXX is this needed with {@link ManagedScheduledExecutorService}?
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Jul 2016]
 */
@ApplicationScoped
public class SchedulerProducer {
	@Inject
	private Logger log;

	/**
	 * 
	 * @return
	 */
	@Produces @ApplicationScoped
	public ScheduledExecutorService scheduler() {
		return Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
	}

	/**
	 * 
	 * @param scheduler
	 */
	public void dispose(@Disposes ScheduledExecutorService scheduler) {
		scheduler.shutdown();
		try {
			if (!scheduler.awaitTermination(1, TimeUnit.MINUTES)) {
				log.warn("Shutdown did not complete in time!");
			}
		} catch (InterruptedException e) {
			log.warn("Shutdown interrupted!", e);
		}
	}
}
