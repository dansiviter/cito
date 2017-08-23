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
package cito.stomp;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import cito.ReflectionUtil;

/**
 * Unit test for {@link HeartBeatMonitor}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Jul 2016]
 */
@RunWith(MockitoJUnitRunner.class)
public class HeartBeatMonitorTest {
	@Mock
	private Connection conn;
	@Mock
	private ScheduledExecutorService scheduler;

	private HeartBeatMonitor monitor;

	@Before
	public void before() {
		this.monitor = new HeartBeatMonitor(this.conn, this.scheduler);
	}

	@Test
	public void start_noReadWrite() {
		this.monitor.start(0, 0);

		verify(this.conn).getSessionId();
	}

	/**
	 * 
	 */
	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void start() {
		final ScheduledFuture<?> read = mock(ScheduledFuture.class);
		when(this.scheduler.schedule(any(Runnable.class), eq(15L), eq(TimeUnit.MILLISECONDS))).thenReturn((ScheduledFuture) read);
		final ScheduledFuture<?> send = mock(ScheduledFuture.class);
		when(this.scheduler.schedule(any(Runnable.class), eq(20L), eq(TimeUnit.MILLISECONDS))).thenReturn((ScheduledFuture) send);

		this.monitor.start(10, 20);

		assertEquals(read, ReflectionUtil.get(this.monitor, "read"));
		assertEquals(send, ReflectionUtil.get(this.monitor, "send"));

		verify(this.conn).getSessionId();
		verify(this.scheduler).schedule(any(Runnable.class), eq(15L), eq(TimeUnit.MILLISECONDS));
		verify(this.scheduler).schedule(any(Runnable.class), eq(20L), eq(TimeUnit.MILLISECONDS));
		verifyNoMoreInteractions(read, send);
	}

	/**
	 * 
	 */
	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void resetRead() {
		final ScheduledFuture<?> read = mock(ScheduledFuture.class);
		ReflectionUtil.set(this.monitor, "read", read);
		ReflectionUtil.set(this.monitor, "readDelay", 10L);
		final ScheduledFuture<?> anotherRead = mock(ScheduledFuture.class);
		when(this.scheduler.schedule(any(Runnable.class), eq(10L), eq(TimeUnit.MILLISECONDS))).thenReturn((ScheduledFuture) anotherRead);

		this.monitor.resetRead();

		assertEquals(anotherRead, ReflectionUtil.get(this.monitor, "read"));

		verify(read).cancel(false);
		verify(this.scheduler).schedule(any(Runnable.class), eq(10L), eq(TimeUnit.MILLISECONDS));
		verifyNoMoreInteractions(read, anotherRead);
	}

	/**
	 * 
	 */
	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void resetSend() {
		final ScheduledFuture<?> send = mock(ScheduledFuture.class);
		ReflectionUtil.set(this.monitor, "send", send);
		ReflectionUtil.set(this.monitor, "sendDelay", 10L);
		final ScheduledFuture<?> anotherSend = mock(ScheduledFuture.class);
		when(this.scheduler.schedule(any(Runnable.class), eq(10L), eq(TimeUnit.MILLISECONDS))).thenReturn((ScheduledFuture) anotherSend);

		this.monitor.resetSend();

		assertEquals(anotherSend, ReflectionUtil.get(this.monitor, "send"));

		verify(send).cancel(false);
		verify(this.scheduler).schedule(any(Runnable.class), eq(10L), eq(TimeUnit.MILLISECONDS));
		verifyNoMoreInteractions(send, anotherSend);
	}

	/**
	 * 
	 */
	@Test
	public void close() {
		final ScheduledFuture<?> send = mock(ScheduledFuture.class);
		ReflectionUtil.set(this.monitor, "send", send);
		final ScheduledFuture<?> read = mock(ScheduledFuture.class);
		ReflectionUtil.set(this.monitor, "read", read);

		this.monitor.close();

		verify(send).cancel(false);
		verify(read).cancel(false);
		verifyNoMoreInteractions(send, read);
	}

	@After
	public void after() {
		verifyNoMoreInteractions(this.conn, this.scheduler);
	}
}
