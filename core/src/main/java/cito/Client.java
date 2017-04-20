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

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cito.server.ws.FrameEncoding;
import cito.stomp.Connection;
import cito.stomp.Frame;
import cito.stomp.HeartBeatMonitor;

/**
 * A basic STOMP WebSocket client.
 * 
 * @author Daniel Siviter
 * @since v1.0 [14 Jul 2016]
 */
@ClientEndpoint(
		subprotocols = { "v11.stomp", "v12.stomp" },
		encoders = FrameEncoding.class,
		decoders = FrameEncoding.class
)
public class Client implements Connection {
	private final static Logger LOG = LoggerFactory.getLogger(Client.class);

	private final Map<Integer, CompletableFuture<Frame>> receipts = new ConcurrentHashMap<>();

	private final AtomicInteger receiptId = new AtomicInteger();
	private final ScheduledExecutorService scheduler;
	private final HeartBeatMonitor heartBeatMonitor;

	private final URI uri;

	private State state = State.DISCONNECTED;
	private Session session;
	private CompletableFuture<Frame> connectFuture;

	public Client(URI uri) {
		this.uri = uri;
		this.scheduler = Executors.newScheduledThreadPool(1);
		this.heartBeatMonitor = new HeartBeatMonitor(this, scheduler);
	}

	@Override
	public String getSessionId() {
		if (getState() == State.DISCONNECTED)
			throw new IllegalStateException("Not connected!");
		return this.session.getId();
	}

	public State getState() {
		return state;
	}

	public void connect(long timeout, TimeUnit unit) throws DeploymentException, IOException, EncodeException, InterruptedException, ExecutionException, TimeoutException {
		if (getState() != State.DISCONNECTED) {
			throw new IllegalStateException("Connection open, or in progress!");
		}
		this.session = ContainerProvider.getWebSocketContainer().connectToServer(this, this.uri);
		this.state = State.CONNECTING;
		final Frame connectFrame = Frame.connect(this.uri.getHost(), "1.2").heartbeat(5_000, 5_000).build();
		sendToClient(connectFrame);
		this.connectFuture = new CompletableFuture<>();
		final Frame connectedFrame = this.connectFuture.get(timeout, unit);
		this.connectFuture = null;

		final long readDelay = Math.max(connectedFrame.heartBeat().x, connectFrame.heartBeat().y);
		final long writeDelay = Math.max(connectFrame.heartBeat().x, connectedFrame.heartBeat().y);
		this.heartBeatMonitor.start(readDelay, writeDelay);
	}

	@Override
	public void sendToClient(Frame frame) throws IOException {
		if (frame.isHeartBeat()) {
			LOG.debug("Sending heart beat.");
		} else {
			LOG.info("Sending frame. [command={}]", frame.getCommand());
		}

		try {
			this.heartBeatMonitor.resetSend();
			this.session.getBasicRemote().sendObject(frame);
		} catch (EncodeException e) {
			throw new IOException(e);
		}
	}

	public String subscribe(String topic, Object listener) {
		throw new UnsupportedOperationException();
	}

	public void unsubscribe(String topic) {
		throw new UnsupportedOperationException();
	}

	@OnMessage
	public void onMessage(Frame frame) {
		this.heartBeatMonitor.resetRead();
		if (frame.isHeartBeat()) {
			LOG.debug("Heartbeart recieved. [sessionId={}]", getSessionId());
			return;
		} else {
			LOG.info("Message recieved! [command={},sessionId={}] {}", frame.getCommand(), getSessionId(), frame);
		}
		switch (frame.getCommand()) {
		case CONNECTED:
			if (getState() != State.CONNECTING || this.connectFuture == null) {
				throw new IllegalStateException("CONNECTED message not expected!");
			}
			this.state = State.CONNECTED;
			this.connectFuture.complete(frame);
			break;
		case MESSAGE:
			System.out.println("MESSAGE recieved!");
			break;
		case RECIEPT:
			this.receipts.get(frame.receiptId()).complete(frame);
			break;
		case ERROR:
			System.out.println("ERROR recieved!");
			try {
				close(new CloseReason(CloseCodes.CLOSED_ABNORMALLY, "STOMP ERROR recieved!"));
			} catch (IOException e) {
				LOG.error("Unable to close!", e);
			}
			break;
		default:
			throw new IllegalStateException("Unexpected command recieved! [" + frame.getCommand() + "]");
		}
	}

	/**
	 * 
	 * @param destination
	 * @param contentType
	 * @param body
	 * @throws IOException
	 */
	public void send(String destination, MediaType contentType, String body) throws IOException {
		sendToClient(Frame.send(destination, contentType, body).build());
	}

	/**
	 * 
	 * @param destination
	 * @param contentType
	 * @param body
	 * @throws IOException
	 * @throws TimeoutException 
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public void send(String destination, MediaType contentType, String body, long timeout, TimeUnit unit)
			throws IOException, InterruptedException, ExecutionException, TimeoutException
	{
		final int receiptId = this.receiptId.incrementAndGet();
		sendToClient(Frame.send(destination, contentType, body).build());
		awaitReceipt(receiptId, timeout, unit);
	}

	/**
	 * 
	 * @param destination
	 * @param contentType
	 * @param body
	 * @param timeout
	 * @param unit
	 * @param fn
	 * @throws IOException
	 * @throws TimeoutException 
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public void send(
			String destination, MediaType contentType, String body, long timeout, TimeUnit unit,
			BiFunction<? super Frame, Throwable, ? extends Frame> fn)
	throws IOException, InterruptedException, ExecutionException, TimeoutException
	{
		final int receiptId = this.receiptId.incrementAndGet();
		sendToClient(Frame.send(destination, contentType, body).reciept(receiptId).build());
		onReceipt(receiptId, timeout, unit, fn);
	}

	/**
	 * Perform graceful shutdown.
	 * 
	 * @param timeout
	 * @param unit
	 * @throws TimeoutException 
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public void disconnect(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		try {
			final int recieptId = this.receiptId.incrementAndGet();
			sendToClient(Frame.disconnect().reciept(recieptId).build());
			awaitReceipt(recieptId, timeout, unit);
			close(new CloseReason(CloseCodes.NORMAL_CLOSURE, null));
		} catch (IOException e) {
			LOG.error("Unable to close!", e);
		}
	}

	/**
	 * Disconnect and don't wait for receipt.
	 */
	public void forceDisconnect() {
		try {
			sendToClient(Frame.disconnect().build());
		} catch (IOException e) {
			LOG.warn("Unable to send DISCONNECT!", e);
		}
		try { // may as well disconnect 
			close(new CloseReason(CloseCodes.NORMAL_CLOSURE, null));
		} catch (IOException e) {
			LOG.error("Unable to close!", e);
		}
	}

	/**
	 * @throws IOException 
	 */
	@Override
	public void close(CloseReason reason) throws IOException {
		if (getState() == State.DISCONNECTED) {
			throw new IllegalStateException();
		}
		try {
			this.heartBeatMonitor.close();
			this.scheduler.shutdown();
			final boolean terminated = this.scheduler.awaitTermination(1, TimeUnit.MINUTES);
			if (!terminated) 
				LOG.warn("Scheduler did not terminate in time!");
		} catch (InterruptedException e) {
			LOG.warn("Thread interruppted!", e);
			Thread.currentThread().interrupt();
		}
		if (this.session != null)
			this.session.close(reason);
		this.session = null;
		this.state = State.DISCONNECTED;
	}

	/**
	 * 
	 * @param receiptId
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	private Frame awaitReceipt(int receiptId, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		final CompletableFuture<Frame> future = new CompletableFuture<>();
		this.receipts.put(receiptId, future);
		return future.get(timeout, unit);
	}

	/**
	 * 
	 * @param receiptId
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	private void onReceipt(int receiptId, long timeout, TimeUnit unit, BiFunction<? super Frame, Throwable, ? extends Frame> fn) throws InterruptedException, ExecutionException, TimeoutException {
		final CompletableFuture<Frame> future = new CompletableFuture<>();
		this.receipts.put(receiptId, future);
		future.handleAsync(fn);
	}


	// --- Static Methods ---

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		final Client client = new Client(URI.create("http://localhost:8080/websocket"));
		client.connect(15, TimeUnit.SECONDS);
		Thread.sleep(15_000);
		client.disconnect(1, TimeUnit.MINUTES);
	}


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [14 Jul 2016]
	 */
	public enum State {
		DISCONNECTED,
		CONNECTING,
		CONNECTED
	}
}
