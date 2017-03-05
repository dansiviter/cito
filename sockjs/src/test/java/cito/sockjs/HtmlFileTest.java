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

import java.io.IOException;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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
@Ignore
public class HtmlFileTest extends AbstractTest {
	private static final String HTML_FILE = "<!doctype html>\n" +
			"<html><head>\n" +
			"  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />\n" +
			"  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
			"</head><body><h2>Don't panic!</h2>\n" +
			"  <script>\n" +
			"    document.domain = document.domain;\n" +
			"    var c = parent.%s;\n" +
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
		final Response res = target("000", uuid, "htmlfile?c=%63allback").request().get();

		assertEquals(Status.OK, res.getStatusInfo());
		assertEquals("text/html;charset=UTF-8", res.getHeaderString(HttpHeaders.CONTENT_TYPE));
		// As HtmlFile is requested using GET we must be very careful not to allow it being cached.
		verifyNotCached(res);

//	        d = r.read()
//	        self.assertEqual(d.strip(), self.head % ('callback',))
//	        self.assertGreater(len(d), 1024)
//	        self.assertEqual(r.read(),
//	                         '<script>\np("o");\n</script>\r\n')
//
//	        r1 = POST(url + '/xhr_send', body='["x"]')
//	        self.assertFalse(r1.body)
//	        self.assertEqual(r1.status, 204)
//
//	        self.assertEqual(r.read(),
//	                         '<script>\np("a[\\"x\\"]");\n</script>\r\n')
//	        r.close()
	}

	/**
	 * Test no callback.
	 */
	@Test
	@RunAsClient
	public void no_callback() throws IOException {
//    def test_no_callback(self):
//	        r = GET(base_url + '/a/a/htmlfile')
//	        self.assertEqual(r.status, 500)
//	        self.assertTrue('"callback" parameter required' in r.body)
	}
	
	/**
	 * Test no response limit.
	 */
	@Test
	@RunAsClient
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
	}


	// --- Static Methods ---

	@Deployment
	public static WebArchive createDeployment() {
		return createWebArchive();
	}
}
