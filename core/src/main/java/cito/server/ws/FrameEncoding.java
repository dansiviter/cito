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
package cito.server.ws;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
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
public class FrameEncoding implements Encoder.TextStream<Frame>, Decoder.TextStream<Frame> {
	@Override
	public void init(EndpointConfig config) { }

	@Override
	public void encode(Frame object, Writer writer) throws EncodeException, IOException {
		object.to(writer);
	}

	@Override
	public Frame decode(Reader reader) throws DecodeException, IOException {
		return Frame.from(reader);
	}

	@Override
	public void destroy() { }
}
