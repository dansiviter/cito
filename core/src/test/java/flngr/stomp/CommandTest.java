package flngr.stomp;

import static flngr.stomp.Command.ABORT;
import static flngr.stomp.Command.ACK;
import static flngr.stomp.Command.BEGIN;
import static flngr.stomp.Command.COMMIT;
import static flngr.stomp.Command.CONNECT;
import static flngr.stomp.Command.CONNECTED;
import static flngr.stomp.Command.DISCONNECT;
import static flngr.stomp.Command.ERROR;
import static flngr.stomp.Command.MESSAGE;
import static flngr.stomp.Command.NACK;
import static flngr.stomp.Command.RECIEPT;
import static flngr.stomp.Command.SEND;
import static flngr.stomp.Command.STOMP;
import static flngr.stomp.Command.SUBSCRIBE;
import static flngr.stomp.Command.UNSUBSCRIBE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import flngr.stomp.Command;

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
		assertTrue(RECIEPT.server());
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
		assertFalse(RECIEPT.destination());
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
		assertFalse(RECIEPT.subscriptionId());
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
		assertFalse(RECIEPT.body());
		assertTrue(SEND.body());
		assertFalse(STOMP.body());
		assertFalse(SUBSCRIBE.body());
		assertFalse(UNSUBSCRIBE.body());
	}
}
