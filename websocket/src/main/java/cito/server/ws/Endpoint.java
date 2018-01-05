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

import javax.websocket.server.ServerEndpoint;

import cito.server.AbstractEndpoint;
import cito.stomp.ws.FrameDecoder;
import cito.stomp.ws.FrameEncoder;

/**
 * Defines a basic WebSocket endpoint.
 * 
 * @author Daniel Siviter
 * @since v1.0 [15 Jul 2016]
 */
@ServerEndpoint(
		value = "/websocket",
		subprotocols = { "v10.stomp", "v11.stomp", "v12.stomp" },
		encoders = { FrameEncoder.Binary.class, FrameEncoder.Text.class },
		decoders = { FrameDecoder.Binary.class, FrameDecoder.Text.class },
		configurator = WebSocketConfigurator.class
)
public class Endpoint extends AbstractEndpoint { }
