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
package cito.stomp.ws;

import static cito.stomp.Encoding.from;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.ByteBuffer;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import cito.stomp.Frame;

/**
 * Handles decoding/encoding of {@link Frame}s.
 * 
 * @author Daniel Siviter
 * @since v1.0 [12 Jul 2016]
 */
public abstract class FrameEncoder implements Encoder {
	private static final int DEFAULT_BUFFER_CAPACITY = 16 * 1024;
	private static final boolean DEFAULT_BUFFER_DIRECT = false;

	protected int bufferCapacity = DEFAULT_BUFFER_CAPACITY;
	protected boolean bufferDirect = DEFAULT_BUFFER_DIRECT;

	protected FrameEncoder() { }

	@Override
	public void init(EndpointConfig config) {
		final Integer bufferCapacity = (Integer) config.getUserProperties().get("stomp.bufferCapacity");
		if (bufferCapacity != null) {
			this.bufferCapacity = bufferCapacity.intValue();
		}
		final Boolean bufferDirect = (Boolean) config.getUserProperties().get("stomp.bufferDirect");
		if (bufferCapacity != null) {
			this.bufferDirect = bufferDirect.booleanValue();
		}
	}

	@Override
	public void destroy() {
		// nothing to destroy
	}


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [29 Nov 2017]
	 */
	public static class Binary extends FrameEncoder implements Encoder.Binary<Frame> {
		@Override
		public ByteBuffer encode(Frame object) throws EncodeException {
			return from(object, this.bufferDirect, this.bufferCapacity);
		}
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [29 Nov 2017]
	 */
	public static class Text extends FrameEncoder implements Encoder.Text<Frame> {
		@Override
		public String encode(Frame object) throws EncodeException {
			return UTF_8.decode(from(object, this.bufferDirect, this.bufferCapacity)).toString();
		}
	}
}
