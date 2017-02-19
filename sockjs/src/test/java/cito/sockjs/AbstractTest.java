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
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContainerInitializer;
import javax.websocket.Endpoint;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.GreaterThan;
import org.wildfly.swarm.spi.api.JARArchive;

import cito.sockjs.jaxrs.JsonMessageBodyReader;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Dec 2016]
 */
@RunWith(Arquillian.class)
public abstract class AbstractTest {
	@ArquillianResource
	private URI deploymenUri;

	private Client client;

	/**
	 * 
	 * @return
	 */
	protected Client createClient() {
		return new ResteasyClientBuilder()
				.socketTimeout(1, TimeUnit.MINUTES)
				.register(JsonMessageBodyReader.class)
				.build();
	}

	/**
	 * 
	 * @return
	 */
	private Client client() {
		return this.client == null ? (this.client = createClient()) : this.client;
	}

	/**
	 * 
	 * @return
	 */
	protected WebTarget target() {
		return client().target(this.deploymenUri).path("test");
	}

	/**
	 * 
	 * @param server
	 * @param session
	 * @param type
	 * @return
	 */
	protected WebTarget target(String server, String session, String type) {
		return target().path(server).path(session).path(type);
	}

	/**
	 * We are going to test several 404/not found pages. We don't define a body or a content type.
	 * 
	 * @param path
	 * @param res
	 */
	protected static void verify404(String path, Response res) {
		assertEquals(path, Status.NOT_FOUND, res.getStatusInfo());
	}

	/**
	 * In some cases 405/method not allowed is more appropriate.
	 * 
	 * @param res
	 */
	protected static void verify405(Response res) {
		assertEquals(Status.METHOD_NOT_ALLOWED, res.getStatusInfo());
		assertNull(res.getHeaderString(HttpHeaders.CONTENT_TYPE));
		assertNotNull(res.getHeaderString(HttpHeaders.ALLOW));
		assertFalse(res.hasEntity());
	}

	/**
	 * Multiple transport protocols need to support OPTIONS method. All responses to OPTIONS requests must be cacheable
	 * and contain appropriate headers.
	 * 
	 * @param path
	 * @param allowedMethods
	 */
	protected void verifyOptions(String path, String... allowedMethods) {
		for (String origin : new String[] { null, "test", "null" }) {
			final Response r = target().path(path).request().header("Origin", origin).options();
			verifyOptions(r, origin, allowedMethods);
		}
	}

	/**
	 * 
	 * @param res
	 * @param origin the origin to test or {@code null}.
	 * @param allowedMethods
	 */
	protected static void verifyOptions(Response res, String origin, String... allowedMethods) {
		assertEquals(Status.NO_CONTENT, res.getStatusInfo());
		assertThat("'max-age' must be large, one year (31536000) is best", res.getHeaderString(HttpHeaders.CACHE_CONTROL), regEx("public, max-age=[1-9][0-9]{6,}"));
		assertNotNull(res.getHeaderString(HttpHeaders.EXPIRES));
		assertThat(Long.parseLong(res.getHeaderString("access-control-max-age")), new GreaterThan<>(1000000L));
		final StringJoiner joiner = new StringJoiner(", ");
		Arrays.asList(allowedMethods).forEach(joiner::add);
		assertEquals(res.getHeaderString("Access-Control-Allow-Methods"), joiner.toString());
		verifyEmptyEntity(res);
		verifyCors(res, origin);
	}

	/**
	 * 
	 * @param res
	 */
	protected static void verifyNoCookie(Response res) {
		assertNull(res.getHeaderString(HttpHeaders.SET_COOKIE));
	}

	/**
	 * Most of the XHR/Ajax based transports do work CORS if proper headers are set.
	 *
	 * @param res
	 * @param origin the origin to test or {@code null}.
	 */
	protected static void verifyCors(Response res, String origin) {
		if (origin != null) {
			assertEquals(origin, res.getHeaderString("access-control-allow-origin"));
		} else {
			assertEquals("*", res.getHeaderString("access-control-allow-origin"));
		}
		// In order to get cookies (JSESSIONID mostly) flying, we need to set allow-credentials header to true.
		assertEquals("true", res.getHeaderString("access-control-allow-credentials"));
	}

	/**
	 * Sometimes, due to transports limitations we need to request private data using GET method. In such case it's very
	 * important to disallow any caching.
	 * 
	 * @param res
	 */
	protected static void verifyNotCached(Response res) {
		assertEquals("no-store, no-cache, must-revalidate, max-age=0", res.getHeaderString(HttpHeaders.CACHE_CONTROL));
		assertNull(res.getHeaderString(HttpHeaders.EXPIRES));
		assertNull(res.getHeaderString(HttpHeaders.LAST_MODIFIED));
	}

	/**
	 * {@link Response#hasEntity()} may actually return {@code true} if it has a media type.
	 * 
	 * @param res
	 */
	protected static void verifyEmptyEntity(Response res) {
		assertTrue(!res.hasEntity() || Util.isEmptyOrNull(res.readEntity(String.class)));
	}

	/**
	 * 
	 * @return
	 */
	protected static String uuid() {
		return UUID.randomUUID().toString();
	}

	/**
	 * 
	 * @return
	 */
	protected static JARArchive createJar() {
		return create(JARArchive.class)
				.addAsServiceProvider(ServletContainerInitializer.class, Initialiser.class)
				.addPackages(true, "cito/sockjs")
				.addAsResource("cito/sockjs", "/cito/sockjs/iframe.html");
	}

	/**
	 * 
	 * @return
	 */
	public static WebArchive createWebArchive() {
		return create(WebArchive.class)
				.addAsLibrary(createJar())
				.addAsLibrary(create(JARArchive.class).addPackages(true, "org/apache/commons/lang3"))
				.addClass(TestConfig.class);
	}


	// --- Inner Classes ---

	/**
	 * Loaded by {@link Initialiser}.
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [4 Jan 2017]
	 */
	public static class TestConfig implements Config {
		@Override
		public String path() {
			return "test";
		}

		@Override
		public Class<? extends Endpoint> endpointClass() {
			return EchoEndpoint.class;
		}
	}
}
