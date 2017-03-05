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

import java.io.IOException;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.junit.Test;

/**
 * Unit tests for {@link JsonPHandler} and {@link JsonPSendHandler}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [1 Mar 2017]
 * @see <a href="https://sockjs.github.io/sockjs-protocol/sockjs-protocol-0.3.3.html#section-111">SockJS 0.3.3 JsonP</a>
 */
public class JsonPTest {
	/**
	 * Test the streaming transport.
	 */
	@Test
	@RunAsClient
	public void transport() throws IOException {
//        url = base_url + '/000/' + str(uuid.uuid4())
//        r = GET(url + '/jsonp?c=%63allback')
//        self.assertEqual(r.status, 200)
//        self.assertEqual(r['Content-Type'],
//                         'application/javascript; charset=UTF-8')
//#
//As JsonPolling is requested using GET we must be very carefull not to allow it being cached.
//
//        self.verify_not_cached(r)
//
//        self.assertEqual(r.body, 'callback("o");\r\n')
//
//        r = POST(url + '/jsonp_send', body='d=%5B%22x%22%5D',
//                 headers={'Content-Type': 'application/x-www-form-urlencoded'})
//#
//Konqueror does weird things on 204. As a workaround we need to respond with something - let it be the string ok.
//
//        self.assertEqual(r.body, 'ok')
//        self.assertEqual(r.status, 200)
//        self.assertEqual(r['Content-Type'], 'text/plain; charset=UTF-8')
//#
//iOS 6 caches POSTs. Make sure we send no-cache header.
//
//        self.verify_not_cached(r)
//
//        r = GET(url + '/jsonp?c=%63allback')
//        self.assertEqual(r.status, 200)
//        self.assertEqual(r.body, 'callback("a[\\"x\\"]");\r\n')
	}
	
	
	/**
	 * Test no callback.
	 */
	@Test
	@RunAsClient
	public void no_callback() throws IOException {
//        r = GET(base_url + '/a/a/jsonp')
//        self.assertEqual(r.status, 500)
//        self.assertTrue('"callback" parameter required' in r.body)
	}
	
	
	/**
	 * Test invalid json.
	 */
	@Test
	@RunAsClient
	public void invalid_json() throws IOException {
//The server must behave when invalid json data is send or when no json data is sent at all.
//        url = base_url + '/000/' + str(uuid.uuid4())
//        r = GET(url + '/jsonp?c=x')
//        self.assertEqual(r.body, 'x("o");\r\n')
//
//        r = POST(url + '/jsonp_send', body='d=%5B%22x',
//                 headers={'Content-Type': 'application/x-www-form-urlencoded'})
//        self.assertEqual(r.status, 500)
//        self.assertTrue("Broken JSON encoding." in r.body)
//
//        for data in ['', 'd=', 'p=p']:
//            r = POST(url + '/jsonp_send', body=data,
//                     headers={'Content-Type': 'application/x-www-form-urlencoded'})
//            self.assertEqual(r.status, 500)
//            self.assertTrue("Payload expected." in r.body)
//
//        r = POST(url + '/jsonp_send', body='d=%5B%22b%22%5D',
//                 headers={'Content-Type': 'application/x-www-form-urlencoded'})
//        self.assertEqual(r.body, 'ok')
//
//        r = GET(url + '/jsonp?c=x')
//        self.assertEqual(r.status, 200)
//        self.assertEqual(r.body, 'x("a[\\"b\\"]");\r\n')
	}

	/**
	 * Test no callback.
	 */
	@Test
	@RunAsClient
	public void content_types() throws IOException {
//The server must accept messages sent with different content types.
//
//    def test_content_types(self):
//        url = base_url + '/000/' + str(uuid.uuid4())
//        r = GET(url + '/jsonp?c=x')
//        self.assertEqual(r.body, 'x("o");\r\n')
//
//        r = POST(url + '/jsonp_send', body='d=%5B%22abc%22%5D',
//                 headers={'Content-Type': 'application/x-www-form-urlencoded'})
//        self.assertEqual(r.body, 'ok')
//        r = POST(url + '/jsonp_send', body='["%61bc"]',
//                 headers={'Content-Type': 'text/plain'})
//        self.assertEqual(r.body, 'ok')
//
//        r = GET(url + '/jsonp?c=x')
//        self.assertEqual(r.status, 200)
//        self.assertEqual(r.body, 'x("a[\\"abc\\",\\"%61bc\\"]");\r\n')
	}
	
	/**
	 * Test no callback.
	 */
	@Test
	@RunAsClient
	public void close() throws IOException {
//        url = close_base_url + '/000/' + str(uuid.uuid4())
//        r = GET(url + '/jsonp?c=x')
//        self.assertEqual(r.body, 'x("o");\r\n')
//
//        r = GET(url + '/jsonp?c=x')
//        self.assertEqual(r.body, 'x("c[3000,\\"Go away!\\"]");\r\n')
//
//        r = GET(url + '/jsonp?c=x')
//        self.assertEqual(r.body, 'x("c[3000,\\"Go away!\\"]");\r\n')
	}
}
