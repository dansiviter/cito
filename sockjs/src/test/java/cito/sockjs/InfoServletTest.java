package cito.sockjs;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * Unit test for {@link InfoServlet}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Dec 2016]
 */
public class InfoServletTest extends AbstractTest {
	/**
	 * This url is called before the client starts the session. It's used to check server capabilities (websocket
	 * support, cookies requirement) and to get the value of "origin" setting (currently not used).
	 * But more importantly, the call to this url is used to measure the roundtrip time between the client and the
	 * server. So, please, do respond to this url in a timely fashion.
	 */
	@Test
	@RunAsClient
	public void test_basic() {
		final Response response = target().request().get();
		assertEquals(200, response.getStatus());
//    def test_basic(self):
//        r = GET(base_url + '/info')
//        self.assertEqual(r.status, 200)
//        self.assertEqual(r['content-type'],
//                         'application/json; charset=UTF-8')
//        self.verify_no_cookie(r)
//        self.verify_not_cached(r)
//        self.verify_cors(r)

//        data = json.loads(r.body)
	// Are websockets enabled on the server?

//        self.assertEqual(data['websocket'], True)
	// Do transports need to support cookies (ie: for load balancing purposes.

//        self.assertTrue(data['cookie_needed'] in  [True, False])
	// List of allowed origins. Currently ignored.

//        self.assertEqual(data['origins'], ['*:*'])
	// Source of entropy for random number generator.

//        self.assertTrue(type(data['entropy']) in [int, long])
	}

	/**
	 * As browsers don't have a good entropy source, the server must help with tht. Info url must supply a good, unpredictable random number from the range <0; 2^32-1> to feed the browser.
	 */
	@Test
	@RunAsClient
	public void test_entropy() {
//    def test_entropy(self):
//        r1 = GET(base_url + '/info')
//        data1 = json.loads(r1.body)
//        r2 = GET(base_url + '/info')
//        data2 = json.loads(r2.body)
//        self.assertTrue(type(data1['entropy']) in [int, long])
//        self.assertTrue(type(data2['entropy']) in [int, long])
//        self.assertNotEqual(data1['entropy'], data2['entropy'])
	}

	/**
	 * Info url must support CORS.
	 */
	@Test
	@RunAsClient
	public void test_options() {
//    def test_options(self):
//        self.verify_options(base_url + '/info', 'OPTIONS, GET')
	}

	/**
	 * SockJS client may be hosted from file:// url. In practice that means the 'Origin' headers sent by the browser will have a value of a string "null". Unfortunately, just echoing back "null" won't work - browser will understand that as a rejection. We must respond with star "*" origin in such case.
	 */
	@Test
	@RunAsClient
	public void test_options_null_origin() {
//    def test_options_null_origin(self):
//            url = base_url + '/info'
//            r = OPTIONS(url, headers={'Origin': 'null'})
//            self.assertEqual(r.status, 204)
//            self.assertFalse(r.body)
//            self.assertEqual(r['access-control-allow-origin'], '*')
	}

	/**
	 * The 'disabled_websocket_echo' service should have websockets disabled.
	 */
	@Test
	@RunAsClient
	public void test_disabled_websocket() {
//    def test_disabled_websocket(self):
//        r = GET(wsoff_base_url + '/info')
//        self.assertEqual(r.status, 200)
//        data = json.loads(r.body)
//        self.assertEqual(data['websocket'], False)
	}


	// --- Static Methods ---

	@Deployment
	public static WARArchive createDeployment() {
		final WARArchive archive = ShrinkWrap.create(WARArchive.class).addClasses(InfoServlet.class, Headers.class);
		archive.addServlet("servlet", InfoServlet.class.getName()).withUrlPattern("/*");
		return archive;
	}
}
