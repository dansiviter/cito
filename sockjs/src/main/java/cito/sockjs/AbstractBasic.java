package cito.sockjs;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;

import javax.websocket.EncodeException;
import javax.websocket.RemoteEndpoint;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [5 Jan 2017]
 */
public abstract class AbstractBasic implements RemoteEndpoint.Basic {
	private boolean batchingAllowed;

	@Override
	public void setBatchingAllowed(boolean allowed) throws IOException {
		batchingAllowed = allowed;
	}

	@Override
	public boolean getBatchingAllowed() {
		return this.batchingAllowed;
	}

	@Override
	public void flushBatch() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendPing(ByteBuffer applicationData) throws IOException, IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendPong(ByteBuffer applicationData) throws IOException, IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendBinary(ByteBuffer data) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendBinary(ByteBuffer partialByte, boolean isLast) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public OutputStream getSendStream() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Writer getSendWriter() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendObject(Object data) throws IOException, EncodeException {
		throw new UnsupportedOperationException();
	}
}
