package civvi.stomp;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [22 Jul 2016]
 */
public class HeartBeatMonitor {
	private static final Logger LOG = LoggerFactory.getLogger(HeartBeatMonitor.class);
	private static final float LATENCY_MULTIPLIER = 1.5f;

	private final Connection conn;
	private final ScheduledExecutorService scheduler;

	private Long sendDelay, readDelay;
	private ScheduledFuture<?> send, read;

	public HeartBeatMonitor(Connection conn, ScheduledExecutorService scheduler) {
		this.conn = conn;
		this.scheduler = scheduler;
	}

	/**
	 * 
	 * @param readDelay
	 * @param writeDelay
	 */
	public void start(long readDelay, long writeDelay) {
		LOG.info("Starting heart beats. [sessionId={},read={},write={},latencyMultiplier={}]",
				this.conn.getSessionId(), readDelay, writeDelay, LATENCY_MULTIPLIER);
		if (readDelay != 0) {
			this.readDelay = (long) (readDelay * LATENCY_MULTIPLIER);
			resetRead();
		}
		if (writeDelay != 0) {
			this.sendDelay = writeDelay;
			resetSend();
		}
	}

	/**
	 * 
	 */
	public void resetRead() {
		// don't do anything if heartbeats are not expected
		if (this.readDelay == null) return;

		if (this.read != null)
			this.read.cancel(false);
		this.read = this.scheduler.schedule(new ReadHeartBeatTimeOut(), this.readDelay, TimeUnit.MILLISECONDS);
	}

	/**
	 * 
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
				LOG.info("Sending heartbeat... [sessionId={}]", conn.getSessionId());
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
