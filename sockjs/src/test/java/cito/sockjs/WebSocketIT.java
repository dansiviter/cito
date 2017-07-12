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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.DeploymentException;
import javax.websocket.Session;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

/**
 * Only RFC-6455 is supported.
 * 
 * @author Daniel Siviter
 * @since v1.0 [1 Mar 2017]
 */
public class WebSocketIT extends AbstractWebSocketIT {
	@Test @RunAsClient
	public void transport() throws DeploymentException, IOException {
		final String uuid = uuid();
		final URI path = uri(EndpointType.ECHO, "000", uuid, "websocket").build();
		final ClientEndpointConfig config = ClientEndpointConfig.Builder.create().build();

		final ClientEndpoint endpoint = new ClientEndpoint();
		try (Session session = this.container.connectToServer(endpoint, config, path)) {
			assertEquals("o", endpoint.get());
			session.getBasicRemote().sendText("a[\"a\"]");
			assertEquals("a[\"a\"]", endpoint.get());
		}
	}

	@Test @RunAsClient
	public void close() throws DeploymentException, IOException {
		final String uuid = uuid();
		final URI path = uri(EndpointType.CLOSE, "000", uuid, "websocket").build();
		final ClientEndpointConfig config = ClientEndpointConfig.Builder.create().build();

		final ClientEndpoint endpoint = new ClientEndpoint();
		try (Session session = this.container.connectToServer(endpoint, config, path)) {
			assertEquals("o", endpoint.get());
			assertEquals("c[3000,\"Go away!\"]", endpoint.get());
			assertTrue(endpoint.wasClosed());
		}
	}

	/**
	 * Empty frames must be ignored by the server side.
	 */
	@Test @RunAsClient
	public void empty_frame() throws DeploymentException, IOException {
		final String uuid = uuid();
		final URI path = uri(EndpointType.ECHO, "000", uuid, "websocket").build();
		final ClientEndpointConfig config = ClientEndpointConfig.Builder.create().build();

		final ClientEndpoint endpoint = new ClientEndpoint();
		try (Session session = this.container.connectToServer(endpoint, config, path)) {
			assertEquals("o", endpoint.get());
			//Server must ignore empty messages.
			session.getBasicRemote().sendText("");
			session.getBasicRemote().sendText("a");
			session.getBasicRemote().sendText("a[\"a\"]");
			assertEquals("a[\"a\"]", endpoint.get());
		}
	}

//For WebSockets, as opposed to other transports, it is valid to reuse session_id. The lifetime of SockJS WebSocket session is defined by a lifetime of underlying WebSocket connection. It is correct to have two separate sessions sharing the same session_id at the same time.
//
//    def test_reuseSessionId(self):
//        on_close = lambda(ws): self.assertFalse(True)
//
//        ws_url = 'ws:' + base_url.split(':',1)[1] + \
//                 '/000/' + str(uuid.uuid4()) + '/websocket'
//        ws1 = websocket.create_connection(ws_url, on_close=on_close)
//        self.assertEqual(ws1.recv(), u'o')
//
//        ws2 = websocket.create_connection(ws_url, on_close=on_close)
//        self.assertEqual(ws2.recv(), u'o')
//
//        ws1.send(u'"a"')
//        self.assertEqual(ws1.recv(), u'a["a"]')
//
//        ws2.send(u'"b"')
//        self.assertEqual(ws2.recv(), u'a["b"]')
//
//        ws1.close()
//        ws2.close()
//#
//It is correct to reuse the same session_id after closing a previous connection.
//
//        ws1 = websocket.create_connection(ws_url)
//        self.assertEqual(ws1.recv(), u'o')
//        ws1.send(u'"a"')
//        self.assertEqual(ws1.recv(), u'a["a"]')
//        ws1.close()

//When user sends broken data - broken JSON for example, the server must abruptly terminate the ws connection.
//
//    def test_broken_json(self):
//        ws_url = 'ws:' + base_url.split(':',1)[1] + \
//                 '/000/' + str(uuid.uuid4()) + '/websocket'
//        ws = websocket.create_connection(ws_url)
//        self.assertEqual(ws.recv(), u'o')
//        ws.send(u'["a')
//        with self.assertRaises(websocket.ConnectionClosedException):
//            if ws.recv() is None:
//                raise websocket.ConnectionClosedException
//        ws.close()


//As a fun part, Firefox 6.0.2 supports Websockets protocol '7'. But, it doesn't send a normal 'Connection: Upgrade' header. Instead it sends: 'Connection: keep-alive, Upgrade'. Brilliant.
//
//    def test_firefox_602_connection_header(self):
//        url = base_url.split(':',1)[1] + \
//            '/000/' + str(uuid.uuid4()) + '/websocket'
//        ws_url = 'ws:' + url
//        http_url = 'http:' + url
//        origin = '/'.join(http_url.split('/')[:3])
//        h = {'Upgrade': 'websocket',
//             'Connection': 'keep-alive, Upgrade',
//             'Sec-WebSocket-Version': '7',
//             'Sec-WebSocket-Origin': 'http://asd',
//             'Sec-WebSocket-Key': 'x3JJHMbDL1EzLkh9GBhXDw==',
//             }
//        r = GET_async(http_url, headers=h)
//        self.assertEqual(r.status, 101)


	// --- Static Methods ---

	@Deployment
	public static WebArchive createDeployment() {
		return createWebArchive();
	}
}
