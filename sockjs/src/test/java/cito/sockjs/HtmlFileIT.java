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

import static cito.sockjs.EventSourceHandler.EVENTSOURCE;
import static cito.sockjs.XhrSendHandler.XHR_SEND;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit test for {@link HtmlFileHandler}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [1 Mar 2017]
 * @see <a href="https://sockjs.github.io/sockjs-protocol/sockjs-protocol-0.3.3.html#section-103">SockJS 0.3.3 HTML File</a>
 */
public class HtmlFileIT extends AbstractIT {
	private static final String HTML_FILE = "<!doctype html>\n" +
			"<html><head>\n" +
			"  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />\n" +
			"  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
			"</head><body><h2>Don't panic!</h2>\n" +
			"  <script>\n" +
			"    document.domain = document.domain;\n" +
			"    var c = parent.callback;\n" +
			"    c.start();\n" +
			"    function p(d) {c.message(d);};\n" +
			"    window.onload = function() {c.stop();};\n" +
			"  </script>";

	/**
	 * Test the streaming transport.
	 */
	@Test
	@RunAsClient
	public void transport() throws IOException {
		final String uuid = uuid();
		final Response res = target("000", uuid, "htmlfile").queryParam("c", "%63allback").request().get();

		assertEquals(Status.OK, res.getStatusInfo());
		assertEquals("text/html;charset=UTF-8", res.getHeaderString(HttpHeaders.CONTENT_TYPE));
		// As HtmlFile is requested using GET we must be very careful not to allow it being cached.
		verifyNotCached(res);

		try (InputStream is = res.readEntity(InputStream.class)) {
			final String d = read(is, 1_024);
			assertTrue(d.length() == 1024);
			assertEquals(HTML_FILE, d.trim());
			assertEquals("<script>\np(\"o\");\n</script>\r\n", read(is, 28));

			final Response res0 = target("000", uuid, XHR_SEND).request().post(Entity.json("[\"x\"]")); 
			assertEquals(Status.NO_CONTENT, res0.getStatusInfo());
			verifyEmptyEntity(res0);

			assertEquals("<script>\np(\"a[\\\"x\\\"]\");\n</script>\r\n", read(is, 35));
		}
	}

	/**
	 * Test no callback.
	 */
	@Test
	@RunAsClient
	public void no_callback() throws IOException {
		final String uuid = uuid();
		final Response res = target("000", uuid, "htmlfile").request().get();

		assertEquals(Status.INTERNAL_SERVER_ERROR, res.getStatusInfo());
		assertEquals("\"callback\" parameter required", res.readEntity(String.class));
	}

	/**
	 * Test no response limit.
	 */
	@Test
	@RunAsClient
	@Ignore
	public void response_limit() throws IOException {
		// Single streaming request should be closed after enough data was delivered (by default 128KiB, but 4KiB for test server).

		//	        url = base_url + '/000/' + str(uuid.uuid4())
		//	        r = GET_async(url + '/htmlfile?c=callback')
		//	        self.assertEqual(r.status, 200)
		//	        self.assertTrue(r.read()) # prelude
		//	        self.assertEqual(r.read(),
		//	                         '<script>\np("o");\n</script>\r\n')
		//	#
		//	Test server should gc streaming session after 4096 bytes were sent (including framing).
		//
		//	        msg = ('x' * 4096)
		//	        r1 = POST(url + '/xhr_send', body='["' + msg + '"]')
		//	        self.assertEqual(r1.status, 204)
		//	        self.assertEqual(r.read(),
		//	                         '<script>\np("a[\\"' + msg + '\\"]");\n</script>\r\n')
		//	#
		//	The connection should be closed after enough data was delivered.
		//
		//	        self.assertFalse(r.read())

		final String uuid = uuid();
		final Response res = target("000", uuid, EVENTSOURCE).request().get();

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

	/**
	 * Reads all the available bytes, essentially to not block on waiting for input.
	 * 
	 * @param is the stream to use as source.
	 * @param limit the limit to the number of bytes.
	 * @return the UTF8 String.
	 * @throws IOException
	 */
	private static String read(InputStream is, int limit) throws IOException {
		if (limit <= 0) {
			throw new IllegalArgumentException("'limit' must be greater than zero!");
		}
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			final byte[] buf = new byte[1_024];
			int read;
			try {
			while (limit != 0 && (read = is.read(buf, 0, buf.length > limit ? limit : buf.length)) >= 0) {
				out.write(buf, 0, read);
				limit -= read;
			}
			} catch (SocketTimeoutException e) {
				System.err.println("XXXXXX '" + UTF_8.decode(ByteBuffer.wrap(out.toByteArray())) + "'");
				throw e;
			}
			return UTF_8.decode(ByteBuffer.wrap(out.toByteArray())).toString();
		}
	}
}
