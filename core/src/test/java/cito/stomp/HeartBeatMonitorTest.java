package cito.stomp;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.concurrent.ScheduledExecutorService;

import org.junit.After;
import org.junit.Before;
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
