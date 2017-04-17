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

import static cito.sockjs.JsonPHandler.JSONP;
import static cito.sockjs.JsonPSendHandler.JSONP_SEND;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

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
 * Unit tests for {@link JsonPHandler} and {@link JsonPSendHandler}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [1 Mar 2017]
 * @see <a href="https://sockjs.github.io/sockjs-protocol/sockjs-protocol-0.3.3.html#section-111">SockJS 0.3.3 JsonP</a>
 */
public class JsonPIT extends AbstractIT {
	/**
	 * Test the streaming transport.
	 */
	@Test
	@RunAsClient
	public void transport() throws IOException {
		final String uuid = uuid();
		Response res = target("000", uuid, JSONP).queryParam("c", "%63allback").request().get();

		assertEquals(Status.OK, res.getStatusInfo());
		assertEquals("application/javascript;charset=UTF-8", res.getHeaderString(HttpHeaders.CONTENT_TYPE));

		// As JsonPolling is requested using GET we must be very careful not to allow it being cached.
		verifyNotCached(res);

		assertEquals("callback(\"o\");\r\n", res.readEntity(String.class));

		final Response res0 = target("000", uuid, JSONP_SEND).request().post(Entity.entity("d=%5B%22x%22%5D", MediaType.APPLICATION_FORM_URLENCODED)); 
		// Konqueror does weird things on 204. As a workaround we need to respond with something - let it be the string ok.
		assertEquals(Status.OK, res0.getStatusInfo());
		assertEquals("ok", res0.readEntity(String.class));
		assertEquals("text/plain;charset=UTF-8", res0.getHeaderString(HttpHeaders.CONTENT_TYPE));

		// iOS 6 caches POSTs. Make sure we send no-cache header.
		verifyNotCached(res0);

		res = target("000", uuid, JSONP).queryParam("c", "%63allback").request().get();
		assertEquals(Status.OK, res.getStatusInfo());
		assertEquals("callback(\"a[\\\"x\\\"]\");\r\n", res.readEntity(String.class));
	}

	/**
	 * Test no callback.
	 */
	@Test
	@RunAsClient
	public void no_callback() throws IOException {
		final Response res = target("a", "a", JSONP).request().get();
		assertEquals(Status.INTERNAL_SERVER_ERROR, res.getStatusInfo());
		assertEquals("\"callback\" parameter required", res.readEntity(String.class));
	}

	/**
	 * Test invalid json.
	 */
	@Test
	@RunAsClient
	public void invalid_json() throws IOException {
		// The server must behave when invalid json data is send or when no json data is sent at all.
		final String uuid = uuid();
		Response res = target("000", uuid, JSONP).queryParam("c", "x").request().get();
		assertEquals("x(\"o\");\r\n", res.readEntity(String.class));

		Response resSend = target("000", uuid, JSONP_SEND).request().post(Entity.entity("d=%5B%22x", MediaType.APPLICATION_FORM_URLENCODED)); 
		assertEquals(Status.INTERNAL_SERVER_ERROR, resSend.getStatusInfo());
		assertEquals("Broken JSON encoding.", resSend.readEntity(String.class));
		assertEquals("text/plain;charset=UTF-8", resSend.getHeaderString(HttpHeaders.CONTENT_TYPE));

		for (String data : new String[] { "", "d=", "p=p" }) {
			resSend = target("000", uuid, JSONP_SEND).request().post(Entity.entity(data, MediaType.APPLICATION_FORM_URLENCODED)); 
			assertEquals(Status.INTERNAL_SERVER_ERROR, resSend.getStatusInfo());
			assertEquals("Payload expected.", resSend.readEntity(String.class));
			assertEquals("text/plain;charset=UTF-8", resSend.getHeaderString(HttpHeaders.CONTENT_TYPE));
		}

		resSend = target("000", uuid, JSONP_SEND).request().post(Entity.entity("d=%5B%22b%22%5D", MediaType.APPLICATION_FORM_URLENCODED)); 
		assertEquals(Status.OK, resSend.getStatusInfo());
		assertEquals("ok", resSend.readEntity(String.class));
		assertEquals("text/plain;charset=UTF-8", resSend.getHeaderString(HttpHeaders.CONTENT_TYPE));

		res = target("000", uuid, JSONP).queryParam("c", "x").request().get();
		assertEquals(Status.OK, res.getStatusInfo());
		assertEquals("x(\"a[\\\"b\\\"]\");\r\n", res.readEntity(String.class));
	}

	/**
	 * Test content types.
	 */
	@Test
	@RunAsClient
	public void content_types() throws IOException {
		// The server must accept messages sent with different content types.
		final String uuid = uuid();
		Response res = target("000", uuid, JSONP).queryParam("c", "x").request().get();
		assertEquals("x(\"o\");\r\n", res.readEntity(String.class));

		Response resSend = target("000", uuid, JSONP_SEND).request().post(Entity.entity("d=%5B%22abc%22%5D", MediaType.APPLICATION_FORM_URLENCODED)); 
		assertEquals(Status.OK, resSend.getStatusInfo());
		assertEquals("ok", resSend.readEntity(String.class));
		
		resSend = target("000", uuid, JSONP_SEND).request().post(Entity.entity("[\"%61bc\"]", MediaType.TEXT_PLAIN)); 
		assertEquals(Status.OK, resSend.getStatusInfo());
		assertEquals("ok", resSend.readEntity(String.class));

		res = target("000", uuid, JSONP).queryParam("c", "x").request().get();
		assertEquals(Status.OK, resSend.getStatusInfo());
		assertEquals("x(\"a[\\\"abc\\\",\\\"%61bc\\\"]\");\r\n", res.readEntity(String.class));
	}

	/**
	 * Test no callback.
	 */
	@Test
	@RunAsClient
	public void close() throws IOException {
		final String uuid = uuid();
		Response res = target(EndpointType.CLOSE, "000", uuid, JSONP).queryParam("c", "x").request().get();
		assertEquals("x(\"o\");\r\n", res.readEntity(String.class));

		res = target(EndpointType.CLOSE, "000", uuid, JSONP).queryParam("c", "x").request().get();
		assertEquals("x(\"c[3000,\\\"Go away!\\\"]\");\r\n", res.readEntity(String.class));

		res = target(EndpointType.CLOSE, "000", uuid, JSONP).queryParam("c", "x").request().get();
		assertEquals("x(\"c[3000,\\\"Go away!\\\"]\");\r\n", res.readEntity(String.class));
	}


	// --- Static Methods ---

	@Deployment
	public static WebArchive createDeployment() {
		return createWebArchive();
	}
}
