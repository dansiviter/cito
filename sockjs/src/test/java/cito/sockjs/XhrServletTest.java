package cito.sockjs;

import static org.junit.Assert.*;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Ignore;
import org.junit.Test;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * Unit test for {@link XhrServlet}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Dec 2016]
 * @see <a href="https://sockjs.github.io/sockjs-protocol/sockjs-protocol-0.3.3.html#section-74">SockJS 0.3.3 XHR Polling</a>
 */
public class XhrServletTest extends AbstractTest {
	private static final String XHR = "xhr";
	private static final String XHR_SEND  = XHR + "_send";

	/**
	 * The transport must support CORS requests, and answer correctly to OPTIONS requests.
	 */
	@Test
	@RunAsClient
	@Ignore
	public void options_xhr() {
		verifyOptions("abc/abc/" + XHR, HttpMethod.OPTIONS, HttpMethod.POST);
	}

	@Test
	@RunAsClient
	@Ignore
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
		Response r = target("000", uuid, XHR).request().post(Entity.json(null));
		assertEquals(Status.OK, r.getStatusInfo());
		assertEquals("o\n", r.readEntity(String.class));
		assertEquals("application/javascript; charset=UTF-8", r.getHeaderString(HttpHeaders.CONTENT_TYPE));
		verifyCors(r, null);
		// iOS 6 caches POSTs. Make sure we send no-cache header.
		verifyNotCached(r);

		// Xhr transports receive json-encoded array of messages.
		r = target("000", uuid, XHR_SEND).request().post(Entity.json("[\"x\"]"));
		//        r = POST(url + '/xhr_send', body='["x"]')
		assertEquals(Status.OK, r.getStatusInfo());
		assertFalse(r.hasEntity());

		// The content type of xhr_send must be set to text/plain, even though the response code is 204. This is due to
		// Firefox/Firebug behaviour - it assumes that the content type is xml and shouts about it.
		assertEquals("text/plain; charset=UTF-8", r.getHeaderString(HttpHeaders.CONTENT_TYPE));
		verifyCors(r, null);
		// iOS 6 caches POSTs. Make sure we send no-cache header.
		verifyNotCached(r);

		r = target("000", uuid, XHR).request().post(Entity.json(null));
		assertEquals(Status.OK, r.getStatusInfo());
		assertEquals("a[\"x\"]\n", r.readEntity(String.class));
	}

	/**
	 * Publishing messages to a non-existing session must result in a 404 error.
	 */
	@Test
	@RunAsClient
	@Ignore
	public void invalidSession() {
		final Response r = target("000", uuid(), XHR_SEND).request().post(Entity.json("[\"x\"]"));
		verify404(r);
	}

	/**
	 * The server must behave when invalid json data is send or when no json data is sent at all.
	 */
	@Test
	@RunAsClient
	@Ignore
	public void invalidJson() {
		final String uuid = uuid();
		Response r = target("000", uuid, XHR).request().post(Entity.json(null));
		assertEquals(Status.OK, r.getStatusInfo());
		assertEquals("o\n", r.readEntity(String.class));

		r = target("000", uuid, XHR_SEND).request().post(Entity.json("[\"x"));
		assertEquals(Status.INTERNAL_SERVER_ERROR, r.getStatusInfo());
		assertEquals("Broken JSON encoding.", r.readEntity(String.class));

		r = target("000", uuid, XHR_SEND).request().post(Entity.json(null));
		assertEquals(Status.INTERNAL_SERVER_ERROR, r.getStatusInfo());
		assertEquals("Payload expected.", r.readEntity(String.class));

		r = target("000", uuid, XHR_SEND).request().post(Entity.json("[\"a\"]"));
		assertEquals(Status.NO_CONTENT, r.getStatusInfo());
		assertEquals("Payload expected.", r.readEntity(String.class));

		r = target("000", uuid, XHR).request().post(Entity.json(null));
		assertEquals(Status.OK, r.getStatusInfo());
		assertEquals("a[\"a\"]\n", r.readEntity(String.class));
	}

	/**
	 * The server must accept messages send with different content types.
	 */
	@Test
	@RunAsClient
	@Ignore
	public void contentType() {
		final String uuid = uuid();
		Response r = target("000", uuid, XHR).request().post(Entity.json(null));
		assertEquals(Status.OK, r.getStatusInfo());
		assertEquals("o\n", r.readEntity(String.class));

		final String[] cTypes = {
				"text/plain",
				"T",
				"application/json", 
				"application/xml",
				"",
				"application/json; charset=utf-8",
				"text/xml; charset=utf-8",
				"text/xml"
		};

		for (String ct : cTypes) {
			r = target("000", uuid, XHR_SEND).request().post(Entity.entity("[\"a\"]", ct));
			assertEquals(Status.NO_CONTENT, r.getStatusInfo());
			assertEquals("o\n", r.readEntity(String.class));
			assertFalse(r.hasEntity());
		}

		r = target("000", uuid, XHR).request().post(Entity.json(null));
		assertEquals(Status.OK, r.getStatusInfo());
		assertEquals("a[\"a\",\"a\"]\n", r.readEntity(String.class));
	}

	/**
	 * When client sends a CORS request with 'Access-Control-Request-Headers' header set, the server must echo back this header as 'Access-Control-Allow-Headers'. This is required in order to get CORS working. Browser will be unhappy otherwise.
	 */
	@Test
	@RunAsClient
	@Ignore
	public void requestHeadersCors() {
		final String uuid = uuid();
		Response r = target("000", uuid, XHR).request().header("Access-Control-Request-Headers", "a, b, c").post(Entity.json(null));
		assertEquals(Status.OK, r.getStatusInfo());
		verifyCors(r, null);
		assertEquals("a, b, c", r.getHeaderString("Access-Control-Allow-Headers"));

		r = target("000", uuid, XHR).request().header("Access-Control-Request-Headers", "").post(Entity.json(null));
		assertEquals(Status.OK, r.getStatusInfo());
		verifyCors(r, null);
		assertNull(r.getHeaderString("Access-Control-Allow-Headers"));

		r = target("000", uuid, XHR).request().post(Entity.json(null));
		assertEquals(Status.OK, r.getStatusInfo());
		verifyCors(r, null);
		assertNull(r.getHeaderString("Access-Control-Allow-Headers"));
	}


	// --- Static Methods ---

	@Deployment
	public static WARArchive createDeployment() {
		final WARArchive archive = ShrinkWrap.create(WARArchive.class).addClasses(IFrameServlet.class, Headers.class);
		archive.addServlet("servlet", GreetingServlet.class.getName()).withUrlPattern("/*");
		return archive;
	}
}
