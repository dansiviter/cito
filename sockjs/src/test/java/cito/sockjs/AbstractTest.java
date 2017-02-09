package cito.sockjs;

import static cito.RegExMatcher.regEx;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.util.Arrays;
import java.util.UUID;

import javax.websocket.Endpoint;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.GreaterThan;
import org.wildfly.swarm.undertow.WARArchive;

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
		return ClientBuilder.newClient();
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
		return client().target(this.deploymenUri);
	}

	/**
	 * 
	 * @param server
	 * @param session
	 * @param type
	 * @return
	 */
	protected WebTarget target(String server, String session, String type) {
		return client().target(this.deploymenUri).path(server).path(session).path(type);
	}

	/**
	 * We are going to test several 404/not found pages. We don't define a body or a content type.
	 * 
	 * @param r
	 */
	protected static void verify404(Response r) {
		assertEquals(Status.NOT_FOUND, r.getStatusInfo());
	}

	/**
	 * In some cases 405/method not allowed is more appropriate.
	 * 
	 * @param r
	 */
	protected static void verify405(Response r) {
		assertEquals(Status.METHOD_NOT_ALLOWED, r.getStatusInfo());
		assertNull(r.getHeaderString(HttpHeaders.CONTENT_TYPE));
		assertNotNull(r.getHeaderString(HttpHeaders.ALLOW));
		assertFalse(r.hasEntity());
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
			verifyOptions(target().path(path).request().header("Origin", origin).options(), origin, allowedMethods);
		}
	}

	/**
	 * 
	 * @param r
	 * @param allowedMethods
	 */
	protected static void verifyOptions(Response r, String origin, String... allowedMethods) {
		assertEquals(Status.NO_CONTENT, r.getStatusInfo());
		assertThat(r.getHeaderString(HttpHeaders.CACHE_CONTROL), regEx("public, max-age=[1-9][0-9]{6}"));
		assertNotNull(r.getHeaderString(HttpHeaders.EXPIRES));
		assertThat(Long.parseLong(r.getHeaderString("access-control-max-age")), new GreaterThan<>(1000000L));
		assertEquals(r.getHeaders().get("Access-Control-Allow-Methods"), Arrays.asList(allowedMethods));
		assertFalse(r.hasEntity());
		verifyCors(r, origin);
	}

	/**
	 * 
	 * @return
	 */
	protected static void verifyNoCookie(Response r) {
		assertNull(r.getHeaderString(HttpHeaders.SET_COOKIE));

	}

	/**
	 * Most of the XHR/Ajax based transports do work CORS if proper headers are set.
	 *
	 * @param r
	 * @param origin
	 */
	protected static void verifyCors(Response r, String origin) {
		if (origin != null && !"null".equals(origin)) {
			assertEquals(r.getHeaderString("access-control-allow-origin"), origin);
		} else {
			assertEquals(r.getHeaderString("access-control-allow-origin"), "*");
		}
		// In order to get cookies (JSESSIONID mostly) flying, we need to set allow-credentials header to true.
		assertEquals("true", r.getHeaderString("access-control-allow-credentials"));
	}

	/**
	 * Sometimes, due to transports limitations we need to request private data using GET method. In such case it's very
	 * important to disallow any caching.
	 * 
	 * @param r
	 */
	protected static void verifyNotCached(Response r) {
		assertEquals("no-store, no-cache, must-revalidate, max-age=0", r.getHeaderString(HttpHeaders.CACHE_CONTROL));
		assertNull(r.getHeaderString(HttpHeaders.EXPIRES));
		assertNull(r.getHeaderString(HttpHeaders.LAST_MODIFIED));
	}

	/**
	 * 
	 * @return
	 */
	protected static String uuid() {
		return UUID.randomUUID().toString();
	}


	// --- Static Methods ---

	@Deployment
	public static WARArchive createDeployment() {
		return ShrinkWrap.create(WARArchive.class)
				.addPackage(SockJsInitialiser.class.getPackage());
//				.addClasses(
//				WebSocketServer.class,
//				AbstractServer.class,
//				WebSocketConfigurator.class,
//				FrameEncoding.class,
//				Frame.class,
//				cito.stomp.Headers.class,
//				WebSocketInitialiser.class,
//				Headers.class);
	}


	// --- Inner Classes ---

	/**
	 * Loaded by {@link SockJsInitialiser}.
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [4 Jan 2017]
	 */
	public static class TestInitialiser implements Initialiser {
		@Override
		public String path() {
			return "echo";
		}

		@Override
		public Class<? extends Endpoint> endpointClass() {
			return EchoEndpoint.class;
		}
	}
}
