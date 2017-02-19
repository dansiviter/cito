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
