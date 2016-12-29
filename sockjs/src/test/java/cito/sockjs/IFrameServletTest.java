package cito.sockjs;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * Unit test for {@link IFrameServlet}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Dec 2016]
 */
public class IFrameServletTest extends AbstractTest{
	/**
	 * SockJS server must provide this html page.
	 */
	@Test
	@RunAsClient
	public void test_simpleUrl() {
//    def test_simpleUrl(self):
//        self.verify(base_url + '/iframe.html')
	}

	/**
	 * To properly utilize caching, the same content must be served for request which try to version the iframe. The
	 * server may want to give slightly different answer for every SockJS client revision.
	 */
	@Test
	@RunAsClient
	public void test_versionedUrl() {
//    def test_versionedUrl(self):
//        for suffix in ['/iframe-a.html', '/iframe-.html', '/iframe-0.1.2.html',
//                       '/iframe-0.1.2abc-dirty.2144.html']:
//            self.verify(base_url + suffix)
	}

	/**
	 * In some circumstances (devel set to true) client library wants to skip caching altogether. That is achieved by
	 * supplying a random query string.
	 */
	@Test
	@RunAsClient
	public void test_queriedUrl() {
//    def test_queriedUrl(self):
//        for suffix in ['/iframe-a.html?t=1234', '/iframe-0.1.2.html?t=123414',
//                       '/iframe-0.1.2abc-dirty.2144.html?t=qweqweq123']:
//            self.verify(base_url + suffix)
	}

	/**
	 * Malformed urls must give 404 answer.
	 */
	@Test
	@RunAsClient
	public void test_invalidUrl() {
//    def test_invalidUrl(self):
//        for suffix in ['/iframe.htm', '/iframe', '/IFRAME.HTML', '/IFRAME',
//                       '/iframe.HTML', '/iframe.xml', '/iframe-/.html']:
//            r = GET(base_url + suffix)
//            self.verify404(r)
	}

	/**
	 * The '/iframe.html' page and its variants must give 200/ok and be served with 'text/html' content type.
	 * The iframe page must be strongly cacheable, supply Cache-Control, Expires and Etag headers and avoid Last-Modified header.
	 * Body must be exactly as specified, with the exception of sockjs_url, which should be configurable.
	 * Sockjs_url must be a valid url and should utilize caching.
	 */
	@Test
	@RunAsClient
	public void verify() {
//    def verify(self, url):
//        r = GET(url)
//        self.assertEqual(r.status, 200)
//        self.assertEqual(r['content-type'], 'text/html; charset=UTF-8')
//
//        self.assertTrue(re.search('public', r['Cache-Control']))
//        self.assertTrue(re.search('max-age=[1-9][0-9]{6}', r['Cache-Control']),
//                        "max-age must be large, one year (31536000) is best")
//        self.assertTrue(r['Expires'])
//	        self.assertTrue(r['ETag'])
//	        self.assertFalse(r['last-modified'])
//
//        match = self.iframe_body.match(r.body.strip())
//        self.assertTrue(match)

//        sockjs_url = match.group('sockjs_url')
//        self.assertTrue(sockjs_url.startswith('/') or
//                        sockjs_url.startswith('http'))
//        self.verify_no_cookie(r)
	}

	/**
	 * The iframe page must be strongly cacheable. ETag headers must not change too often. Server must support 'if-none-match' requests.
	 */
	@Test
	@RunAsClient
	public void test_cacheability() {
//    def test_cacheability(self):
//        r1 = GET(base_url + '/iframe.html')
//        r2 = GET(base_url + '/iframe.html')
//        self.assertEqual(r1['etag'], r2['etag'])
//        self.assertTrue(r1['etag']) # Let's make sure ETag isn't None.
//
//        r = GET(base_url + '/iframe.html', headers={'If-None-Match': r1['etag']})
//        self.assertEqual(r.status, 304)
//        self.assertFalse(r['content-type'])
//        self.assertFalse(r.body)
	}


	// --- Static Methods ---

	@Deployment
	public static WARArchive createDeployment() {
		final WARArchive archive = ShrinkWrap.create(WARArchive.class).addClasses(IFrameServlet.class, Headers.class);
		archive.addServlet("servlet", GreetingServlet.class.getName()).withUrlPattern("/*");
		return archive;
	}
}
