package cito.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.PongMessage;
import javax.websocket.Session;

import org.slf4j.Logger;

/**
 * Uses WebSocket ping/pong to calculate Round Trip Time (RTT).
 * 
 * @author Daniel Siviter
 * @since v1.0 [30 Jan 2018]
 *
 */
@ApplicationScoped
class RttService {
	private static final Duration RTT_DELAY = Duration.ofMinutes(10);

	@Inject
	private Logger log;
	@Resource 
	private ManagedScheduledExecutorService executor;

	/**
	 * 
	 * @param session
	 */
	void start(Session session) {
		this.executor.submit(() -> ping(session));
	}

	/**
	 * 
	 * @param session
	 */
	private void ping(Session session) {
		if (!session.isOpen()) {
			return;
		}
		try {
			this.log.debug("Sending ping. [id={}]", session.getId());
			final long now = System.currentTimeMillis();
			final ByteBuffer buf = (ByteBuffer) ByteBuffer.allocate(Long.BYTES).putLong(now).flip();
			session.getBasicRemote().sendPing(buf);
		} catch (IOException e) {
			log.warn("Problems with ping/pong!", e);
		}
	}

	/**
	 * 
	 * @param session
	 * @param msg
	 */
	void pong(Session session, PongMessage msg) {
		final long now = System.currentTimeMillis();
		final ByteBuffer buf = msg.getApplicationData();
		final long rtt = now - buf.getLong();
		this.log.info("RTT {}ms. [id={}]", rtt, session.getId());
		session.getUserProperties().put("RTT", rtt);
		this.executor.schedule(() -> ping(session), RTT_DELAY.toMillis(), TimeUnit.MILLISECONDS);
	}
}
