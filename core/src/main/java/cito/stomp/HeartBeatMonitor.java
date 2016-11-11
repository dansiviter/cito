package cito.stomp;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * TODO:
 *  o Configurable latency/flutter multiplier
 * 
 * @author Daniel Siviter
 * @since v1.0 [22 Jul 2016]
 */
public class HeartBeatMonitor {
	private static final Logger LOG = LogManager.getLogger(HeartBeatMonitor.class);
	private static final float LATENCY_MULTIPLIER = 1.5f;

	private final Connection conn;
	private final ScheduledExecutorService scheduler;

	private Long sendDelay, readDelay;
	private ScheduledFuture<?> send, read;

	/**
	 * 
	 * @param conn
	 * @param scheduler the scheduler to perform heartbeat tasks. This class will not be responsible for shutting this
	 * down.
	 */
	public HeartBeatMonitor(Connection conn, ScheduledExecutorService scheduler) {
		this.conn = conn;
		this.scheduler = scheduler;
	}

	/**
	 * 
	 * @param read the read delay in milliseconds.
	 * @param write the write delay in milliseconds.
	 */
	public void start(long read, long write) {
		LOG.info("Starting heart beats. [sessionId={},read={},write={},latencyMultiplier={}]",
				this.conn.getSessionId(), read, write, LATENCY_MULTIPLIER);
		if (read != 0) {
			this.readDelay = (long) (read * LATENCY_MULTIPLIER);
			resetRead();
		}
		if (write != 0) {
			this.sendDelay = write;
			resetSend();
		}
	}

	/**
	 * Reset the read timeout.
	 */
	public void resetRead() {
		// don't do anything if heartbeats are not expected
		if (this.readDelay == null) return;

		if (this.read != null)
			this.read.cancel(false);
		this.read = this.scheduler.schedule(new ReadHeartBeatTimeOut(), this.readDelay, TimeUnit.MILLISECONDS);
	}

	/**
	 * Reset the write timeout.
	 */
	public void resetSend() {
		// don't do anything if heartbeats are not expected
		if (this.sendDelay == null) return;

		if (this.send != null)
			this.send.cancel(false);
		this.send = this.scheduler.schedule(new SendHeartBeat(), this.sendDelay, TimeUnit.MILLISECONDS);
	}

	/**
	 * 
	 */
	public void close() {
		if (this.sendDelay != null)
			this.send.cancel(false);
		if (this.readDelay != null)
			this.read.cancel(false);
	}


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [22 Jul 2016]
	 */
	private class SendHeartBeat implements Runnable {
		@Override
		public void run() {
			try {
				LOG.debug("Sending heartbeat... [sessionId={}]", conn.getSessionId());
				conn.send(Frame.HEART_BEAT);
			} catch (IOException | RuntimeException e) {
				LOG.warn("Unable to send heartbeat!", e);
			}
		}
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [22 Jul 2016]
	 */
	private class ReadHeartBeatTimeOut implements Runnable {
		@Override
		public void run() {
			try {
				LOG.error("No read heartbeat! Closing... [sessionId={}]", conn.getSessionId());
				conn.close(new CloseReason(CloseCodes.VIOLATED_POLICY, "Heartbeat not recieved in time."));
			} catch (IOException | RuntimeException e) {
				LOG.warn("Unable to close!", e);
			}
		}
	}
}
