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

import static cito.sockjs.XhrHandler.XHR;
import static cito.sockjs.XhrSendHandler.XHR_SEND;
import static cito.sockjs.XhrStreamingHandler.XHR_STREAMING;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.client.Entity.json;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

/**
 * Unit test for {@link XhrHandler}, {@link XhrSendHandler} and {@link XhrStreamingHandler}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Dec 2016]
 * @see <a href="https://sockjs.github.io/sockjs-protocol/sockjs-protocol-0.3.3.html#section-74">SockJS 0.3.3 XHR Polling</a>
 * @see <a href="https://sockjs.github.io/sockjs-protocol/sockjs-protocol-0.3.3.html#section-85">SockJS 0.3.3 XHR Streaming</a>
 */
public class XhrIT extends AbstractIT {
	/**
	 * The transport must support CORS requests, and answer correctly to OPTIONS requests.
	 */
	@Test
	@RunAsClient
	public void options_xhr() {
		verifyOptions("abc/abc/" + XHR, HttpMethod.POST, HttpMethod.OPTIONS);
	}

	@Test
	@RunAsClient
	public void options_xhrSend() {
		verifyOptions("abc/abc/" + XHR_SEND, HttpMethod.POST, HttpMethod.OPTIONS);
	}

	@Test
	@RunAsClient
	public void options_xhrStreaming() {
		verifyOptions("abc/abc/" + XHR_STREAMING, HttpMethod.POST, HttpMethod.OPTIONS);
	}

	/**
	 * Test the polling transport.
	 */
	@Test
	@RunAsClient
	public void transport_polling() {
		final String uuid = uuid();
		try (ClosableResponse res = post(target("000", uuid, XHR), json(null))) {
			assertEquals(Status.OK, res.getStatusInfo());
			assertEquals("o\n", res.readEntity(String.class));
			assertEquals("application/javascript;charset=UTF-8", res.getHeaderString(HttpHeaders.CONTENT_TYPE));
			verifyCors(res, null);
			// iOS 6 caches POSTs. Make sure we send no-cache header.
			verifyNotCached(res);
		}

		// Xhr transports receive json-encoded array of messages.
		try (ClosableResponse res = post(target("000", uuid, XHR_SEND), json("[\"x\"]"))) {
			assertEquals(Status.NO_CONTENT, res.getStatusInfo());
			verifyEmptyEntity(res);

			// The content type of xhr_send must be set to text/plain, even though the response code is 204. This is due to
			// Firefox/Firebug behaviour - it assumes that the content type is xml and shouts about it.
			assertEquals("text/plain;charset=UTF-8", res.getHeaderString(HttpHeaders.CONTENT_TYPE));
			verifyCors(res, null);
			// iOS 6 caches POSTs. Make sure we send no-cache header.
			verifyNotCached(res);
		}

		try (ClosableResponse res = post(target("000", uuid, XHR), json(null))) {
			assertEquals(Status.OK, res.getStatusInfo());
			assertEquals("a[\"x\"]\n", res.readEntity(String.class));
		}
	}

	/**
	 * Test the streaming transport.
	 * @throws IOException 
	 */
	@Test
	@RunAsClient
	public void transport_streaming() throws IOException {
		final String uuid = uuid();
		try (ClosableResponse res = post(target("000", uuid, XHR_STREAMING), json(null))) {

			assertEquals(Status.OK, res.getStatusInfo());
			assertEquals("application/javascript;charset=UTF-8", res.getHeaderString(HttpHeaders.CONTENT_TYPE));
			verifyCors(res, null);
			// iOS 6 caches POSTs. Make sure we send no-cache header.
			verifyNotCached(res);

			// The transport must first send 2KiB of h bytes as prelude.
			try (BufferedReader reader = toReader(res.readEntity(InputStream.class))) {
				assertEquals(StringUtils.leftPad("", 2048, "h"), reader.readLine());
				assertEquals("o", reader.readLine());

				try (ClosableResponse res0 = post(target("000", uuid, XHR_SEND), json("[\"x\"]"))) {
					assertEquals(Status.NO_CONTENT, res0.getStatusInfo());
					verifyEmptyEntity(res0);
				}

				assertEquals("a[\"x\"]", reader.readLine());
			}
		}
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
		try (ClosableResponse res = post(target("000", uuid, XHR_STREAMING), json(null))) {

			try (Scanner scanner = new Scanner(res.readEntity(InputStream.class), "UTF8")) {
				scanner.useDelimiter("\n");
				assertEquals(StringUtils.leftPad("", 2048, "h"), scanner.next());
				assertEquals("o", scanner.next());

				// Test server should gc streaming session after 4096 bytes were sent (including framing).
				final String msg = StringUtils.leftPad("", 128, "x");
				for (int i = 0; i < 31; i++) {
					try (ClosableResponse res0 = post(target("000", uuid, XHR_SEND), json("[\"" + msg + "\"]"))) {
						assertEquals(Status.NO_CONTENT, res0.getStatusInfo());
						verifyEmptyEntity(res0);
					}
					assertEquals("Iteration " + i, "a[\"" + msg + "\"]", scanner.next());
				}
				// The connection should be closed after enough data was delivered.
				assertFalse(scanner.hasNext());
			}
		}
	}


	/**
	 * Publishing messages to a non-existing session must result in a 404 error.
	 */
	@Test
	@RunAsClient
	public void invalidSession() {
		try (ClosableResponse res = post(target("000", uuid(), XHR_SEND), json("[\"x\"]"))) {
			verify404(XHR_SEND, res);
		}
	}

	/**
	 * The server must behave when invalid json data is send or when no json data is sent at all.
	 */
	@Test
	@RunAsClient
	public void invalidJson() {
		final String uuid = uuid();
		try (ClosableResponse res = post(target("000", uuid, XHR), json(null))) {
			assertEquals(Status.OK, res.getStatusInfo());
			assertEquals("o\n", res.readEntity(String.class));
		}
		try (ClosableResponse res = post(target("000", uuid, XHR_SEND), json("[\"x"))) {
			assertEquals(Status.INTERNAL_SERVER_ERROR, res.getStatusInfo());
			assertEquals("Broken JSON encoding.", res.readEntity(String.class));
		}
		try (ClosableResponse res = post(target("000", uuid, XHR_SEND), json(null))) {
			assertEquals(Status.INTERNAL_SERVER_ERROR, res.getStatusInfo());
			assertEquals("Payload expected.", res.readEntity(String.class));
		}
		try (ClosableResponse res = post(target("000", uuid, XHR_SEND), json("[\"a\"]"))) {
			assertEquals(Status.NO_CONTENT, res.getStatusInfo());
			verifyEmptyEntity(res);
		}
		try (ClosableResponse res = post(target("000", uuid, XHR), json(null))) {
			assertEquals(Status.OK, res.getStatusInfo());
			assertEquals("a[\"a\"]\n", res.readEntity(String.class));
		}
	}

	/**
	 * The server must accept messages send with different content types.
	 */
	@Test
	@RunAsClient
	public void contentType() {
		final String uuid = uuid();
		try (ClosableResponse res = post(target("000", uuid, XHR), json(null))) {
			assertEquals(Status.OK, res.getStatusInfo());
			assertEquals("o\n", res.readEntity(String.class));
		}

		final String[] cTypes = {
				"text/plain",
				//				"T", // FIXME unable to parse this ATM so cannot test
				"application/json", 
				"application/xml",
				//				"", // FIXME
				"application/json; charset=utf-8",
				"text/xml; charset=utf-8",
				"text/xml"
		};

		for (String ct : cTypes) {
			try (ClosableResponse res = post(target("000", uuid, XHR_SEND), entity("[\"a\"]", MediaType.valueOf(ct)))) {
				assertEquals(Status.NO_CONTENT, res.getStatusInfo());
				verifyEmptyEntity(res);
			}
		}

		try (ClosableResponse res = post(target("000", uuid, XHR), json(null))) {
			assertEquals(Status.OK, res.getStatusInfo());
			assertEquals("a[\"a\",\"a\",\"a\",\"a\",\"a\",\"a\"]\n", res.readEntity(String.class));
		}
	}

	/**
	 * When client sends a CORS request with 'Access-Control-Request-Headers' header set, the server must echo back this header as 'Access-Control-Allow-Headers'. This is required in order to get CORS working. Browser will be unhappy otherwise.
	 */
	@Test
	@RunAsClient
	public void requestHeadersCors() {
		final String uuid = uuid();
		try (ClosableResponse res = post(target("000", uuid, XHR).request().header("Access-Control-Request-Headers", "a, b, c"), json(null))) {
			assertEquals(Status.OK, res.getStatusInfo());
			verifyCors(res, null);
			assertEquals("a, b, c", res.getHeaderString("Access-Control-Allow-Headers"));
		}
		try (ClosableResponse res = post(target("000", uuid, XHR).request().header("Access-Control-Request-Headers", ""), json(null))) {
			assertEquals(Status.OK, res.getStatusInfo());
			verifyCors(res, null);
			assertNull(res.getHeaderString("Access-Control-Allow-Headers"));
		}
		try (ClosableResponse res = post(target("000", uuid, XHR), json(null))) {
			assertEquals(Status.OK, res.getStatusInfo());
			verifyCors(res, null);
			assertNull(res.getHeaderString("Access-Control-Allow-Headers"));
		}
	}


	// --- Static Methods ---

	@Deployment
	public static WebArchive createDeployment() {
		return createWebArchive();
	}
}
