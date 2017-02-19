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

import static cito.RegExMatcher.regEx;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

/**
 * Unit test for {@link IFrameHandler}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Dec 2016]
 * @see <a href="https://sockjs.github.io/sockjs-protocol/sockjs-protocol-0.3.3.html#section-15">SockJS 0.3.3 iFrame</a>
 */
public class IFrameTest extends AbstractTest{
	/**
	 * SockJS server must provide this html page.
	 */
	@Test
	@RunAsClient
	public void test_simpleUrl() {
		verify("iframe.html", null);
	}

	/**
	 * To properly utilize caching, the same content must be served for request which try to version the iframe. The
	 * server may want to give slightly different answer for every SockJS client revision.
	 */
	@Test
	@RunAsClient
	public void test_versionedUrl() {
		final String[] suffixes = {
				"iframe-a.html",
				"iframe-.html", 
				"iframe-0.1.2.html",
				"iframe-0.1.2abc-dirty.2144.html"
		};

		for (String suffix : suffixes) {
			verify(suffix, null);
		}
	}

	/**
	 * In some circumstances (devel set to true) client library wants to skip caching altogether. That is achieved by
	 * supplying a random query string.
	 */
	@Test
	@RunAsClient
	public void test_queriedUrl() {
		final String[][] suffixes = {
				{ "iframe-a.html", "t=1234" },
				{ "iframe-0.1.2.html", "t=123414" }, 
				{ "iframe-0.1.2abc-dirty.2144.html", "t=qweqweq123" }
		};

		for (String[] suffix : suffixes) {
			verify(suffix[0], suffix[1]);
		}
	}

	/**
	 * Malformed urls must give 404 answer.
	 */
	@Test
	@RunAsClient
	public void test_invalidUrl() {
		final String[] suffixes = {
				"iframe.htm",
				"iframe", 
				"IFRAME.HTML", 
				"IFRAME", 
				"iframe.HTML", 
				"iframe.xml", 
				"iframe-/.html"
		};

		for (String suffix : suffixes) {
			final Response res = target().path(suffix).request().get();
			verify404(suffix, res);
			res.close();
		}
	}

	/**
	 * The '/iframe.html' page and its variants must give 200/ok and be served with 'text/html' content type.
	 * The iframe page must be strongly cacheable, supply Cache-Control, Expires and Etag headers and avoid Last-Modified header.
	 * Body must be exactly as specified, with the exception of sockjs_url, which should be configurable.
	 * Sockjs_url must be a valid url and should utilize caching.
	 */
	public void verify(String suffix, String query) {
		final Response res = target().path(suffix).request().get();
		assertEquals(suffix, Status.OK, res.getStatusInfo());
		assertEquals("text/html;charset=UTF-8", res.getHeaderString(HttpHeaders.CONTENT_TYPE));

		assertThat("'max-age' must be large, one year (31536000) is best", res.getHeaderString(HttpHeaders.CACHE_CONTROL), regEx("public, max-age=[1-9][0-9]{6,}"));
		assertNotNull(res.getHeaderString(HttpHeaders.EXPIRES));

		assertNotNull(res.getEntityTag());
		assertNull(res.getHeaderString(HttpHeaders.LAST_MODIFIED));

//        match = self.iframe_body.match(r.body.strip())
//        self.assertTrue(match)

//        sockjs_url = match.group('sockjs_url')
//        self.assertTrue(sockjs_url.startswith('/') or
//                        sockjs_url.startswith('http'))
		verifyNoCookie(res);
		res.close();
	}

	/**
	 * The iframe page must be strongly cacheable. ETag headers must not change too often. Server must support 'if-none-match' requests.
	 */
	@Test
	@RunAsClient
	public void test_cacheability() {
		Response res = target().path("iframe.html").request().get();
		final EntityTag eTag0 = res.getEntityTag();
		res.close();
		res = target().path("iframe.html").request().get();
		final EntityTag eTag1 = res.getEntityTag();
		res.close();
		assertEquals(eTag0, eTag1);
		
		res = target().path("iframe.html").request().header(HttpHeaders.IF_NONE_MATCH, eTag0.getValue()).get();
		assertEquals(Status.NOT_MODIFIED, res.getStatusInfo());
		verifyEmptyEntity(res);
	}


	// --- Static Methods ---

	@Deployment
	public static WebArchive createDeployment() {
		return createWebArchive();
	}
}
