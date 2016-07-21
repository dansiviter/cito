package civvi.stomp;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import civvi.messaging.annotation.OnConnect;

/**
 * A basic STOMP WebSocket client.
 * 
 * @author Daniel Siviter
 * @since v1.0 [14 Jul 2016]
 */
@ClientEndpoint(subprotocols = "STOMP", encoders = FrameEncoding.class, decoders = FrameEncoding.class)
public class Client implements Closeable {
	private final static Logger LOG = LoggerFactory.getLogger(Client.class);

	private final AtomicInteger recieptId = new AtomicInteger();
	private final URI uri;
	private State state = State.DISCONNECTED;
	private Session session;

	private CompletableFuture<Frame> connectFuture;

	public Client(URI uri) {
		this.uri = uri;
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
		send(Frame.connect(this.uri.getHost()).header(Headers.ACCEPT_VERSION, "1.2").build());
		this.connectFuture = new CompletableFuture<>();
		this.connectFuture.get(timeout, unit);
		this.connectFuture = null;
	}

	private void send(Frame frame) throws IOException, EncodeException {
		this.session.getBasicRemote().sendObject(frame);
	}

	public String subscribe(String topic, Object listener) {
		throw new UnsupportedOperationException();
	}

	public void unsubscribe(String topic) {
		throw new UnsupportedOperationException();
	}
	
	@OnConnect
	public void onConnect(Session session) {
		this.session = session;
	}

	@OnMessage
	public void onMessage(Frame frame) {
		LOG.info("Message recieved! [command={}]", frame.getCommand());
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
		case RECEIPT:
			System.out.println("RECEIPT recieved!");
			break;
		case ERROR:
			System.out.println("ERROR recieved!");
			try {
				close();
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
	 * @throws EncodeException
	 */
	public void send(String destination, MediaType contentType, String body) throws IOException, EncodeException {
		send(Frame.send(destination, contentType, body).build());
	}

	/**
	 * Perform graceful shutdown.
	 * 
	 * @param time
	 * @param unit
	 */
	public void disconnect(long time, TimeUnit unit) {
		try {
			send(Frame.disconnect().reciept(recieptId.incrementAndGet()).build());
		} catch (IOException | EncodeException e) {
			LOG.warn("Unable to send DISCONNECT!", e);
		}
		try { // may as well disconnect 
			close();
		} catch (IOException e) {
			LOG.error("Unable to close!", e);
		}
	}

	/**
	 * Disconnect and don't wait for receipt.
	 */
	public void forceDisconnect() {
		try {
			send(Frame.disconnect().build());
		} catch (IOException | EncodeException e) {
			LOG.warn("Unable to send DISCONNECT!", e);
		}
		try { // may as well disconnect 
			close();
		} catch (IOException e) {
			LOG.error("Unable to close!", e);
		}
	}

	/**
	 * @throws IOException 
	 */
	@Override
	public void close() throws IOException {
		if (getState() == State.DISCONNECTED) {
			throw new IllegalStateException();
		}
		if (this.session != null)
			this.session.close();
		this.session = null;
		this.state = State.DISCONNECTED;
	}

	public static void main(String[] args) throws Exception {
		try (final Client client = new Client(URI.create("http://localhost:8080/websocket"))) {
			client.connect(15, TimeUnit.SECONDS);
			
			Thread.sleep(10_000);
		}
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
