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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.concurrent.Future;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.junit.Ignore;
import org.junit.Test;

/**
 * 
 * 
 * @author Daniel Siviter
 * @since v1.0 [3 Jan 2017]
 * @see <a href="https://sockjs.github.io/sockjs-protocol/sockjs-protocol-0.3.3.html#section-42">SockJS 0.3.3 Framing</a>
 */
public class FramingTest extends AbstractTest {
	private static final String XHR = "xhr";
	private static final String XHR_SEND  = XHR + "_send";

	/**
	 * When server receives a request with unknown session_id it must recognize that as request for a new session. When server opens a new sesion it must immediately send an frame containing a letter o.
	 */
	@Test
	@RunAsClient
	@Ignore
	public void simpleSession() {
		final String uuid = uuid();
		Response r = target("000", uuid, XHR).request().post(Entity.json(null));

		// New line is a frame delimiter specific for xhr-polling"
		assertEquals(Status.OK, r.getStatusInfo());
		assertEquals("o\n", r.readEntity(String.class));

		// After a session was established the server needs to accept requests for sending messages.

		// Xhr-polling accepts messages as a list of JSON-encoded strings.
		r = target("000", uuid, XHR_SEND).request().post(Entity.json("[\"a\"]"));
		assertEquals(Status.NO_CONTENT, r.getStatusInfo());
		assertFalse(r.hasEntity());

		// We're using an echo service - we'll receive our message back. The message is encoded as an array 'a'.
		r = target("000", uuid, XHR).request().post(Entity.json(null));
		assertEquals(Status.OK, r.getStatusInfo());
		assertEquals("a[\"a\"]\n", r.readEntity(String.class));
				
		// Sending messages to not existing sessions is invalid.
		r = target("000", "bad_session", XHR_SEND).request().post(Entity.json("[\"a\"]"));
		verify404(XHR_SEND, r);

		// The session must time out after 5 seconds of not having a receiving connection. The server must send a
		// heartbeat frame every 25 seconds. The heartbeat frame contains a single h character. This delay may be
		// configurable.

		// TODO

		// The server must not allow two receiving connections to wait on a single session. In such case the server must
		// send a close frame to the new connection.

		Future<Response> asyncFuture = target("000", uuid, XHR).request().async().post(Entity.json(null));
		target("000", uuid, XHR).request().post(Entity.json(null));
		assertEquals(Status.OK, r.getStatusInfo());
		assertEquals("c[2010,\"Another connection still open\"]\n", r.readEntity(String.class));

		asyncFuture.cancel(true);

		// 
	}

	/**
	 * The server may terminate the connection, passing error code and message.
	 */
	@Test
	@RunAsClient
	@Ignore
	public void closeSession() {
		final String uuid = uuid();
		Response r = target().path("close").path("000").path(uuid).path(XHR).request().post(Entity.json(null));
		assertEquals(Status.OK, r.getStatusInfo());
		assertEquals("o\n", r.readEntity(String.class));

		r = target().path("close").path("000").path(uuid).path(XHR).request().post(Entity.json(null));
		assertEquals(Status.OK, r.getStatusInfo());
		assertEquals("c[3000,\"Go away!\"]\n", r.readEntity(String.class));

		// Until the timeout occurs, the server must constantly serve the close message.
		r = target().path("close").path("000").path(uuid).path(XHR).request().post(Entity.json(null));
		assertEquals(Status.OK, r.getStatusInfo());
		assertEquals("c[3000,\"Go away!\"]\n", r.readEntity(String.class));
	}
}
