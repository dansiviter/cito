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

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.spec.WebArchive;
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

	/**
	 * The transport must support CORS requests, and answer correctly to OPTIONS requests.
	 */
	@Test
	@RunAsClient
	public void options_xhr() {
		verifyOptions("abc/abc/" + XHR, HttpMethod.OPTIONS, HttpMethod.POST);
	}

	@Test
	@RunAsClient
	public void options_xhrSend() {
		verifyOptions("abc/abc/" + XHR_SEND, HttpMethod.OPTIONS, HttpMethod.POST);
	}

	/**
	 * Test the transport itself.
	 */
	@Test
	@RunAsClient
	public void transport() {
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

	@Deployment
	public static WebArchive createDeployment() {
		return createWebArchive();
	}
}
