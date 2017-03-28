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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 * @author Daniel Siviter
 * @since v1.0 [28 Mar 2017]
 */
@RunWith(MockitoJUnitRunner.class)
public class SchedulerProducerTest {
	@Mock
	private Logger log;

	@InjectMocks
	private SchedulerProducer schedulerProducer;

	/**
	 * 
	 * @param scheduler
	 * @throws InterruptedException 
	 */
	@Test
	public void dispose() throws InterruptedException {
		final ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
		when(scheduler.awaitTermination(1, TimeUnit.MINUTES)).thenReturn(true);

		this.schedulerProducer.dispose(scheduler);

		verify(scheduler).shutdown();
		verify(scheduler).awaitTermination(1, TimeUnit.MINUTES);
		verifyNoMoreInteractions(scheduler);
	}

	/**
	 * 
	 * @param scheduler
	 * @throws InterruptedException 
	 */
	@Test
	public void dispose_timeout() throws InterruptedException {
		final ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);

		this.schedulerProducer.dispose(scheduler);

		verify(scheduler).shutdown();
		verify(scheduler).awaitTermination(1, TimeUnit.MINUTES);
		verify(this.log).warn("Shutdown did not complete in time!");
		verifyNoMoreInteractions(scheduler);
	}

	/**
	 * 
	 * @param scheduler
	 * @throws InterruptedException 
	 */
	@Test
	public void dispose_interrupted() throws InterruptedException {
		final ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
		final InterruptedException e = new InterruptedException();
		when(scheduler.awaitTermination(1, TimeUnit.MINUTES)).thenThrow(e);

		this.schedulerProducer.dispose(scheduler);

		verify(scheduler).shutdown();
		verify(scheduler).awaitTermination(1, TimeUnit.MINUTES);
		verify(this.log).warn("Shutdown interrupted!", e);
		verifyNoMoreInteractions(scheduler);
	}

	@After
	public void after() {
		verifyNoMoreInteractions(this.log);
	}
}
