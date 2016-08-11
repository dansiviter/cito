package flngr;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Jul 2016]
 */
public class SchedulerProvider {
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
	}
}
