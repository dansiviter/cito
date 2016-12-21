package cito;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.slf4j.Logger;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Jul 2016]
 */
public class SchedulerProvider {
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
