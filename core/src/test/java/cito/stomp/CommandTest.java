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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cito.stomp.Command;

/**
 * Unit test for {@link Command}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Jul 2016]
 */
public class CommandTest {
	@Test
	public void server() {
		assertFalse(ABORT.server());
		assertFalse(ACK.server());
		assertFalse(BEGIN.server());
		assertFalse(COMMIT.server());
		assertFalse(CONNECT.server());
		assertTrue(CONNECTED.server());
		assertFalse(DISCONNECT.server());
		assertTrue(ERROR.server());
		assertTrue(MESSAGE.server());
		assertFalse(NACK.server());
		assertTrue(RECEIPT.server());
		assertFalse(SEND.server());
		assertFalse(STOMP.server());
		assertFalse(SUBSCRIBE.server());
		assertFalse(UNSUBSCRIBE.server());
	}

	@Test
	public void destination() {
		assertFalse(ABORT.destination());
		assertFalse(ACK.destination());
		assertFalse(BEGIN.destination());
		assertFalse(COMMIT.destination());
		assertFalse(CONNECT.destination());
		assertFalse(CONNECTED.destination());
		assertFalse(DISCONNECT.destination());
		assertFalse(ERROR.destination());
		assertTrue(MESSAGE.destination());
		assertFalse(NACK.destination());
		assertFalse(RECEIPT.destination());
		assertTrue(SEND.destination());
		assertFalse(STOMP.destination());
		assertTrue(SUBSCRIBE.destination());
		assertFalse(UNSUBSCRIBE.destination());
	}

	@Test
	public void subscriptionId() {
		assertFalse(ABORT.subscriptionId());
		assertFalse(ACK.subscriptionId());
		assertFalse(BEGIN.subscriptionId());
		assertFalse(COMMIT.subscriptionId());
		assertFalse(CONNECT.subscriptionId());
		assertFalse(CONNECTED.subscriptionId());
		assertFalse(DISCONNECT.subscriptionId());
		assertFalse(ERROR.subscriptionId());
		assertTrue(MESSAGE.subscriptionId());
		assertFalse(NACK.subscriptionId());
		assertFalse(RECEIPT.subscriptionId());
		assertFalse(SEND.subscriptionId());
		assertFalse(STOMP.subscriptionId());
		assertTrue(SUBSCRIBE.subscriptionId());
		assertTrue(UNSUBSCRIBE.subscriptionId());
	}

	@Test
	public void body() {
		assertFalse(ABORT.body());
		assertFalse(ACK.body());
		assertFalse(BEGIN.body());
		assertFalse(COMMIT.body());
		assertFalse(CONNECT.body());
		assertFalse(CONNECTED.body());
		assertFalse(DISCONNECT.body());
		assertTrue(ERROR.body());
		assertTrue(MESSAGE.body());
		assertFalse(NACK.body());
		assertFalse(RECEIPT.body());
		assertTrue(SEND.body());
		assertFalse(STOMP.body());
		assertFalse(SUBSCRIBE.body());
		assertFalse(UNSUBSCRIBE.body());
	}
}
