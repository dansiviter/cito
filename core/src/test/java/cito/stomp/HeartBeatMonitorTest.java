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

import static org.junit.Assert.fail;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.concurrent.ScheduledExecutorService;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import cito.stomp.Connection;
import cito.stomp.HeartBeatMonitor;

/**
 * Unit test for {@link HeartBeatMonitor}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Jul 2016]
 */
@RunWith(MockitoJUnitRunner.class) @Ignore
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

	/**
	 * 
	 */
	@Test
	public void start() {
		this.monitor.start(0, 0);
		fail();
	}

	/**
	 * 
	 */
	@Test
	public void resetRead() {
		this.monitor.resetRead();
		fail();
	}

	/**
	 * 
	 */
	@Test
	public void resetSend() {
		this.monitor.resetSend();
		fail();
	}

	/**
	 * 
	 */
	@Test
	public void close() {
		this.monitor.close();
		fail();
	}

	@After
	public void after() {
		verifyNoMoreInteractions(this.conn, this.scheduler);
	}
}
