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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit test for {@link XhrHandler} and {@link XhrSendHandler}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Dec 2016]
 * @see <a href="https://sockjs.github.io/sockjs-protocol/sockjs-protocol-0.3.3.html#section-74">SockJS 0.3.3 XHR Polling</a>
 */
public class XhrTest extends AbstractTest {
	private static final String XHR = "xhr";
	private static final String XHR_SEND  = XHR + "_send";
	private static final String XHR_STREAMING  = XHR + "_streaming";

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
		Response res = target("000", uuid, XHR).request().post(Entity.json(null));
		assertEquals(Status.OK, res.getStatusInfo());
		assertEquals("o\n", res.readEntity(String.class));
		assertEquals("application/javascript;charset=UTF-8", res.getHeaderString(HttpHeaders.CONTENT_TYPE));
		verifyCors(res, null);
		// iOS 6 caches POSTs. Make sure we send no-cache header.
		verifyNotCached(res);

		// Xhr transShrinkports receive json-encoded array of messages.
		res = target("000", uuid, XHR_SEND).request().post(Entity.json("[\"x\"]")); 
		assertEquals(Status.NO_CONTENT, res.getStatusInfo());
		verifyEmptyEntity(res);

		// The content type of xhr_send must be set to text/plain, even though the response code is 204. This is due to
		// Firefox/Firebug behaviour - it assumes that the content type is xml and shouts about it.
		assertEquals("text/plain;charset=UTF-8", res.getHeaderString(HttpHeaders.CONTENT_TYPE));
		verifyCors(res, null);
		// iOS 6 caches POSTs. Make sure we send no-cache header.
		verifyNotCached(res);

		res = target("000", uuid, XHR).request().post(Entity.json(null));
		assertEquals(Status.OK, res.getStatusInfo());
		assertEquals("a[\"x\"]\n", res.readEntity(String.class));
	}

	/**
	 * Test the streaming transport.
	 * @throws IOException 
	 */
	@Test
	@RunAsClient
	@Ignore
	public void transport_streaming() throws IOException {
		final String uuid = uuid();
		Response res = target("000", uuid, XHR_STREAMING).request().post(Entity.json(null));
		assertEquals(Status.OK, res.getStatusInfo());
		assertEquals("application/javascript;charset=UTF-8", res.getHeaderString(HttpHeaders.CONTENT_TYPE));
		verifyCors(res, null);
		// iOS 6 caches POSTs. Make sure we send no-cache header.
		verifyNotCached(res);

		// The transport must first send 2KiB of h bytes as prelude.
		final InputStream entity = res.readEntity(InputStream.class);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(entity))) {
			assertEquals(StringUtils.leftPad("", 2048, "h"), reader.readLine());
			assertEquals("o", reader.readLine());

			final Response res0 = target("000", uuid, XHR_SEND).request().post(Entity.json("[\"x\"]")); 
			assertEquals(Status.NO_CONTENT, res0.getStatusInfo());
			verifyEmptyEntity(res0);

			assertEquals("a[\"x\"]\n", reader.readLine());
			res0.close();
		}
	}

	@Test
	@RunAsClient
	public void response_limit() {
		// Single streaming request will buffer all data until closed. In order to remove (garbage collect) old messages from the browser memory we should close the connection every now and then. By default we should close a streaming request every 128KiB messages was send. The test server should have this limit decreased to 4096B.

		//		url = base_url + '/000/' + str(uuid.uuid4())
		//		r = POST_async(url + '/xhr_streaming')
		//		self.assertEqual(r.status, 200)
		//		self.assertTrue(r.read()) # prelude
		//		self.assertEqual(r.read(), 'o\n')

		//Test server should gc streaming session after 4096 bytes were sent (including framing).

		//		msg = '"' + ('x' * 128) + '"'
		//		for i in range(31):
		//			r1 = POST(url + '/xhr_send', body='[' + msg + ']')
		//			self.assertEqual(r1.status, 204)
		//			self.assertEqual(r.read(), 'a[' + msg + ']\n')
		// The connection should be closed after enough data was delivered.

		//			self.assertFalse(r.read())
	}



	/**
	 * Publishing messages to a non-existing session must result in a 404 error.
	 */
	@Test
	@RunAsClient
	public void invalidSession() {
		final Response res = target("000", uuid(), XHR_SEND).request().post(Entity.json("[\"x\"]"));
		verify404(XHR_SEND, res);
	}

	/**
	 * The server must behave when invalid json data is send or when no json data is sent at all.
	 */
	@Test
	@RunAsClient
	public void invalidJson() {
		final String uuid = uuid();
		Response res = target("000", uuid, XHR).request().post(Entity.json(null));
		assertEquals(Status.OK, res.getStatusInfo());
		assertEquals("o\n", res.readEntity(String.class));

		res = target("000", uuid, XHR_SEND).request().post(Entity.json("[\"x"));
		assertEquals(Status.INTERNAL_SERVER_ERROR, res.getStatusInfo());
		assertEquals("Broken JSON encoding.", res.readEntity(String.class));

		res = target("000", uuid, XHR_SEND).request().post(Entity.json(null));
		assertEquals(Status.INTERNAL_SERVER_ERROR, res.getStatusInfo());
		assertEquals("Payload expected.", res.readEntity(String.class));

		res = target("000", uuid, XHR_SEND).request().post(Entity.json("[\"a\"]"));
		assertEquals(Status.NO_CONTENT, res.getStatusInfo());
		verifyEmptyEntity(res);

		res = target("000", uuid, XHR).request().post(Entity.json(null));
		assertEquals(Status.OK, res.getStatusInfo());
		assertEquals("a[\"a\"]\n", res.readEntity(String.class));
	}

	/**
	 * The server must accept messages send with different content types.
	 */
	@Test
	@RunAsClient
	public void contentType() {
		final String uuid = uuid();
		Response res = target("000", uuid, XHR).request().post(Entity.json(null));
		assertEquals(Status.OK, res.getStatusInfo());
		assertEquals("o\n", res.readEntity(String.class));

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
			res = target("000", uuid, XHR_SEND).request().post(Entity.entity("[\"a\"]", MediaType.valueOf(ct)));
			assertEquals(Status.NO_CONTENT, res.getStatusInfo());
			verifyEmptyEntity(res);
		}

		res = target("000", uuid, XHR).request().post(Entity.json(null));
		assertEquals(Status.OK, res.getStatusInfo());
		assertEquals("a[\"a\",\"a\",\"a\",\"a\",\"a\",\"a\"]\n", res.readEntity(String.class));
	}

	/**
	 * When client sends a CORS request with 'Access-Control-Request-Headers' header set, the server must echo back this header as 'Access-Control-Allow-Headers'. This is required in order to get CORS working. Browser will be unhappy otherwise.
	 */
	@Test
	@RunAsClient
	public void requestHeadersCors() {
		final String uuid = uuid();
		Response r = target("000", uuid, XHR).request().header("Access-Control-Request-Headers", "a, b, c").post(Entity.json(null));
		assertEquals(Status.OK, r.getStatusInfo());
		verifyCors(r, null);
		assertEquals("a, b, c", r.getHeaderString("Access-Control-Allow-Headers"));
		r.close();

		r = target("000", uuid, XHR).request().header("Access-Control-Request-Headers", "").post(Entity.json(null));
		assertEquals(Status.OK, r.getStatusInfo());
		verifyCors(r, null);
		assertNull(r.getHeaderString("Access-Control-Allow-Headers"));
		r.close();

		r = target("000", uuid, XHR).request().post(Entity.json(null));
		assertEquals(Status.OK, r.getStatusInfo());
		verifyCors(r, null);
		assertNull(r.getHeaderString("Access-Control-Allow-Headers"));
	}


	// --- Static Methods ---

	@Deployment
	public static WebArchive createDeployment() {
		return createWebArchive();
	}
}
