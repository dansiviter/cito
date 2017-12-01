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
package cito.stomp;

import static cito.stomp.Command.ABORT;
import static cito.stomp.Command.ACK;
import static cito.stomp.Command.BEGIN;
import static cito.stomp.Command.COMMIT;
import static cito.stomp.Command.CONNECT;
import static cito.stomp.Command.CONNECTED;
import static cito.stomp.Command.DISCONNECT;
import static cito.stomp.Command.ERROR;
import static cito.stomp.Command.MESSAGE;
import static cito.stomp.Command.NACK;
import static cito.stomp.Command.RECEIPT;
import static cito.stomp.Command.SEND;
import static cito.stomp.Command.STOMP;
import static cito.stomp.Command.SUBSCRIBE;
import static cito.stomp.Command.UNSUBSCRIBE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import javax.ws.rs.core.MediaType;

import org.junit.Test;

/**
 * Defines a STOMP frame
 * 
 * @author Daniel Siviter
 * @since v1.0 [12 Jul 2016]
 */
public class FrameTest {
	@Test
	public void toString_() {
		final Frame frame0 = Frame.message("/wonderland", "sub-0", "123", MediaType.TEXT_PLAIN_TYPE, "body").build();
		assertEquals("MESSAGE\ndestination:/wonderland\nsubscription:sub-0\nmessage-id:123\ncontent-length:4\ncontent-type:text/plain\n\nbody\u0000", frame0.toString());

		final Frame frame1 = Frame.send("/wonderland", MediaType.TEXT_PLAIN_TYPE, "body").build();
		assertEquals("SEND\ndestination:/wonderland\ncontent-length:4\ncontent-type:text/plain\n\nbody\u0000", frame1.toString());
	}

	@Test
	public void builder_body() {
		final Command[] commands = {
				ABORT,
				ACK,
				BEGIN,
				COMMIT,
				CONNECT,
				CONNECTED,
				DISCONNECT,
				NACK,
				RECEIPT,
				STOMP,
				SUBSCRIBE,
				UNSUBSCRIBE
		};

		for (Command command : commands) {
			try {
				Frame.builder(command).body(MediaType.TEXT_PLAIN_TYPE, "");
				fail("IllegalArgumentException expected!");
			} catch (RuntimeException e) {
				assertEquals(IllegalArgumentException.class, e.getClass());
				assertEquals(command + " does not accept a body!", e.getMessage());
			}
		}

		for (Command command : new Command[] { SEND, MESSAGE, ERROR }) {
			assertNotNull(Frame.builder(command).body(MediaType.TEXT_PLAIN_TYPE, "blagh"));
		}
	}
}
