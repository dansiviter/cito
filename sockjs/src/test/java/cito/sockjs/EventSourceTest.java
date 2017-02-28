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
import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

/**
 * Unit test for {@link EventSourceHandler}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Feb 2017]
 * @see <a href="https://sockjs.github.io/sockjs-protocol/sockjs-protocol-0.3.3.html#section-94">SockJS 0.3.3 EventSource</a>
 */
public class EventSourceTest extends AbstractTest {
	private static final String XHR_SEND  = "xhr_send";
	private static final String EVENT_SOURCE = "eventsource";

	@Test
	@RunAsClient
	public void options_eventSource() {
		verifyOptions("abc/abc/" + EVENT_SOURCE, HttpMethod.GET, HttpMethod.OPTIONS);
	}

	/**
	 * Test the streaming transport.
	 */
	@Test
	@RunAsClient
	public void transport() throws IOException {
		final String uuid = uuid();
		final Response res = target("000", uuid, EVENT_SOURCE).request().get();

		assertEquals(Status.OK, res.getStatusInfo());
		assertEquals("text/event-stream;charset=UTF-8", res.getHeaderString(HttpHeaders.CONTENT_TYPE));
		// As EventSource is requested using GET we must be very carefull not to allow it being cached.
		verifyNotCached(res);

		// The transport must first send a new line prelude, due to a bug in Opera.
		final InputStream is = res.readEntity(InputStream.class);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
			assertEquals("", reader.readLine());
			assertEquals("data: o", reader.readLine());
			assertEquals("", reader.readLine());

			final Response res0 = target("000", uuid, XHR_SEND).request().post(Entity.json("[\"x\"]")); 
			assertEquals(Status.NO_CONTENT, res0.getStatusInfo());
			verifyEmptyEntity(res0);
			res0.close();

			assertEquals("data: a[\"x\"]", reader.readLine());
			assertEquals("", reader.readLine());

			// This protocol doesn't allow binary data and we need to specially treat leading space, new lines and
			// things like \x00. But, now the protocol json-encodes everything, so there is no way to trigger this case.
			final Response res1 = target("000", uuid, XHR_SEND).request().post(Entity.json("[\"  \\u0000\\n\\r \"]")); 
			assertEquals(Status.NO_CONTENT, res1.getStatusInfo());
			verifyEmptyEntity(res1);
			res1.close();

			assertEquals("data: a[\"  \\u0000\\n\\r \"]", reader.readLine());
			assertEquals("", reader.readLine());
		}
		res.close();
	}

	/**
	 * Single streaming request will buffer all data until closed. In order to remove (garbage collect) old messages
	 * from the browser memory we should close the connection every now and then. By default we should close a
	 * streaming request every 128KiB messages was send. The test server should have this limit decreased to 4096B.
	 */
	@Test
	@RunAsClient
	public void response_limit() throws IOException {
		final String uuid = uuid();
		final Response res = target("000", uuid, EVENT_SOURCE).request().get();

		final InputStream is = res.readEntity(InputStream.class);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
			assertEquals("", reader.readLine());
			assertEquals("data: o", reader.readLine());
			assertEquals("", reader.readLine());

			// Test server should gc streaming session after 4096 bytes were sent (including framing).

			final String msg = StringUtils.leftPad("", 4096, "x");
			final Response res0 = target("000", uuid, XHR_SEND).request().post(Entity.json("[\"" + msg + "\"]")); 
			assertEquals(Status.NO_CONTENT, res0.getStatusInfo());
			verifyEmptyEntity(res0);
			res0.close();
			assertEquals("data: a[\"" + msg + "\"]", reader.readLine());
			assertEquals("", reader.readLine());
			// The connection should be closed after enough data was delivered.
			assertNull(reader.readLine());
			res.close();
		}
	}


	// --- Static Methods ---

	@Deployment
	public static WebArchive createDeployment() {
		return createWebArchive();
	}
}
