package cito.sockjs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * Unit test for {@link GreetingHandler}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Dec 2016]
 * @see <a href="https://sockjs.github.io/sockjs-protocol/sockjs-protocol-0.3.3.html#section-12">SockJS 0.3.3 Greeting</a>
 */
public class GreetingTest extends AbstractTest {
	/**
	 * The most important part of the url scheme, is without doubt, the top url. Make sure the greeting is valid.
	 */
	@Test
	@RunAsClient
	public void test_greeting() {
		final Response response = target().request().get();
		assertEquals(200, response.getStatus());
		assertEquals(MediaType.valueOf("text/plain; charset=UTF-8"), response.getMediaType());
		assertEquals("Welcome to SockJS!\n", response.readEntity(String.class));
		assertTrue(response.getCookies().isEmpty());
	}

	/**
	 * Other simple requests should return 404.
	 */
	@Test
	@RunAsClient
	public void notFound() {
		notFound("a");
		notFound("a.html");
//		notFound("/", "/");			// JAXRS will ignore this!
//		notFound("/", "/", "/");	// JAXRS will ignore this!
		notFound("a", "a");
		notFound("a", "a", "/");
		notFound("a", "/");
	}

	private void notFound(String... paths) {
		WebTarget target = target();
		for (String path : paths) {
			target = target.path(path);
		}
		System.out.println(target.getUri());
		final Response res = target.request().get();
		assertEquals(404, res.getStatus());
		res.close();
	}


	// --- Static Methods ---

	@Deployment
	public static WebArchive createDeployment() {
		return createWebArchive();
	}
}
