package cito.sockjs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * Unit test for {@link GreetingServlet}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Dec 2016]
 */
public class GreetingServletTest extends AbstractTest {
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
	public void test_notFound() {
		final Response response0 = target().path("a").request().get();
		assertEquals(404, response0.getStatus());
		final Response response1 = target().path("a.html").request().get();
		assertEquals(404, response1.getStatus());
		final Response response2 = target().path("/").request().get();
		assertEquals(404, response2.getStatus());
		final Response response3 = target().path("//").request().get();
		assertEquals(404, response3.getStatus());
		final Response response4 = target().path("a/a").request().get();
		assertEquals(404, response4.getStatus());
		final Response response5 = target().path("a/a/").request().get();
		assertEquals(404, response5.getStatus());
		final Response response6 = target().path("a/").request().get();
		assertEquals(404, response6.getStatus());
	}

	@Deployment
	public static WARArchive createDeployment() {
		final WARArchive archive = ShrinkWrap.create(WARArchive.class).addClasses(GreetingServlet.class, Headers.class);
		archive.addServlet("servlet", GreetingServlet.class.getName()).withUrlPattern("/*");
		return archive;
	}
}
