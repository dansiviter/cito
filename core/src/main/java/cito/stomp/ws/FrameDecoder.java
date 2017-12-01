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

import static cito.stomp.Encoding.NULL;
import static cito.stomp.Encoding.from;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import cito.stomp.Frame;

/**
 * Handles decoding/encoding of {@link Frame}s.
 * <p/>
 * <string>Note:</strong> The STOMP specification states "servers MAY place maximum limits" on certain elements. For
 * simplicity sake this is ignored as the WebSocket implementation will have a maximum buffer size. If that's breached
 * then it makes it a moot point of any limits in here. e.g. for Undertow this is 16kb.
 * 
 * @author Daniel Siviter
 * @since v1.0 [12 Jul 2016]
 */
public abstract class FrameDecoder implements Decoder {
	protected FrameDecoder() { }

	@Override
	public void init(EndpointConfig config) {
		// nothing to initialise
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
	public static class Binary extends FrameDecoder implements Decoder.Binary<Frame> {
		@Override
		public boolean willDecode(ByteBuffer bytes) {
			return bytes.get(bytes.limit()) == NULL;
		}

		@Override
		public Frame decode(ByteBuffer bytes) throws DecodeException {
			try {
				return from(bytes);
			} catch (IOException e) {
				throw new DecodeException(bytes, e.getMessage(), e);
			}
		}
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [29 Nov 2017]
	 */
	public static class Text extends FrameDecoder implements Decoder.Text<Frame> {
		@Override
		public boolean willDecode(String s) {
			return s.charAt(s.length() - 1) == NULL;
		}

		@Override
		public Frame decode(String s) throws DecodeException {
			try {
				return from(UTF_8.encode(s));
			} catch (IOException e) {
				throw new DecodeException(s, e.getMessage(), e);
			}
		}
	}
}
