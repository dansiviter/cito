package civvi.stomp.server;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.websocket.CloseReason;
import javax.websocket.Extension;
import javax.websocket.MessageHandler;
import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

public abstract class AbstractSession implements Session {

	@Override
	public WebSocketContainer getContainer() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addMessageHandler(MessageHandler handler) throws IllegalStateException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<MessageHandler> getMessageHandlers() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeMessageHandler(MessageHandler handler) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getProtocolVersion() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getNegotiatedSubprotocol() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Extension> getNegotiatedExtensions() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSecure() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isOpen() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getMaxIdleTimeout() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setMaxIdleTimeout(long milliseconds) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setMaxBinaryMessageBufferSize(int length) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getMaxBinaryMessageBufferSize() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setMaxTextMessageBufferSize(int length) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getMaxTextMessageBufferSize() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Async getAsyncRemote() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Basic getBasicRemote() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close(CloseReason closeReason) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public URI getRequestURI() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, List<String>> getRequestParameterMap() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getQueryString() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, String> getPathParameters() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, Object> getUserProperties() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Principal getUserPrincipal() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Session> getOpenSessions() {
		throw new UnsupportedOperationException();
	}
}
