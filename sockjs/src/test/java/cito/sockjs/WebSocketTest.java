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

/**
 * @author Daniel Siviter
 * @since v1.0 [1 Mar 2017]
 */
public class WebSocketTest {
//    def test_httpMethod(self):
//        r = GET(base_url + '/0/0/websocket')
//        self.assertEqual(r.status, 400)
//        self.assertTrue('Can "Upgrade" only to "WebSocket".' in r.body)
//#
//Some proxies and load balancers can rewrite 'Connection' header, in such case we must refuse connection.
//
//    def test_invalidConnectionHeader(self):
//        r = GET(base_url + '/0/0/websocket', headers={'Upgrade': 'WebSocket',
//                                                      'Connection': 'close'})
//        self.assertEqual(r.status, 400)
//        self.assertTrue('"Connection" must be "Upgrade".', r.body)
//#
//WebSocket should only accept GET
//
//    def test_invalidMethod(self):
//        for h in [{'Upgrade': 'WebSocket', 'Connection': 'Upgrade'},
//                  {}]:
//            r = POST(base_url + '/0/0/websocket', headers=h)
//            self.verify405(r)
//#
//Support WebSocket Hixie-76 protocol
//
//class WebsocketHixie76(Test):
//#
//    def test_transport(self):
//        ws_url = 'ws:' + base_url.split(':',1)[1] + \
//                 '/000/' + str(uuid.uuid4()) + '/websocket'
//        ws = websocket.create_connection(ws_url)
//        self.assertEqual(ws.recv(), u'o')
//        ws.send(u'["a"]')
//        self.assertEqual(ws.recv(), u'a["a"]')
//        ws.close()
//#
//    def test_close(self):
//        ws_url = 'ws:' + close_base_url.split(':',1)[1] + \
//                 '/000/' + str(uuid.uuid4()) + '/websocket'
//        ws = websocket.create_connection(ws_url)
//        self.assertEqual(ws.recv(), u'o')
//        self.assertEqual(ws.recv(), u'c[3000,"Go away!"]')
//#
//The connection should be closed after the close frame.
//
//        with self.assertRaises(websocket.ConnectionClosedException):
//            if ws.recv() is None:
//                raise websocket.ConnectionClosedException
//        ws.close()
//#
//Empty frames must be ignored by the server side.
//
//    def test_empty_frame(self):
//        ws_url = 'ws:' + base_url.split(':',1)[1] + \
//                 '/000/' + str(uuid.uuid4()) + '/websocket'
//        ws = websocket.create_connection(ws_url)
//        self.assertEqual(ws.recv(), u'o')
//#
//Server must ignore empty messages.
//
//        ws.send(u'')
//        ws.send(u'["a"]')
//        self.assertEqual(ws.recv(), u'a["a"]')
//        ws.close()
//#
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
//#
//Verify WebSocket headers sanity. Due to HAProxy design the websocket server must support writing response headers before receiving -76 nonce. In other words, the websocket code must work like that:
//
//Receive request headers.
//Write response headers.
//Receive request nonce.
//Write response nonce.
//    def test_haproxy(self):
//        url = base_url.split(':',1)[1] + \
//                 '/000/' + str(uuid.uuid4()) + '/websocket'
//        ws_url = 'ws:' + url
//        http_url = 'http:' + url
//        origin = '/'.join(http_url.split('/')[:3])
//
//        c = RawHttpConnection(http_url)
//        r = c.request('GET', http_url, http='1.1', headers={
//                'Connection':'Upgrade',
//                'Upgrade':'WebSocket',
//                'Origin': origin,
//                'Sec-WebSocket-Key1': '4 @1  46546xW%0l 1 5',
//                'Sec-WebSocket-Key2': '12998 5 Y3 1  .P00'
//                })
//#
//First check response headers
//
//        self.assertEqual(r.status, 101)
//        self.assertEqual(r.headers['connection'].lower(), 'upgrade')
//        self.assertEqual(r.headers['upgrade'].lower(), 'websocket')
//        self.assertEqual(r.headers['sec-websocket-location'], ws_url)
//        self.assertEqual(r.headers['sec-websocket-origin'], origin)
//        self.assertFalse('Content-Length' in r.headers)
//#
//Later send token
//
//        c.send('aaaaaaaa')
//        self.assertEqual(c.read()[:16],
//                         '\xca4\x00\xd8\xa5\x08G\x97,\xd5qZ\xba\xbfC{')
//#
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
//#
//The server must support Hybi-10 protocol
//
//class WebsocketHybi10(Test):
//#
//    def test_transport(self):
//        trans_url = base_url + '/000/' + str(uuid.uuid4()) + '/websocket'
//        ws = WebSocket8Client(trans_url)
//
//        self.assertEqual(ws.recv(), 'o')
//#
//Server must ignore empty messages.
//
//        ws.send(u'')
//        ws.send(u'["a"]')
//        self.assertEqual(ws.recv(), 'a["a"]')
//        ws.close()
//#
//    def test_close(self):
//        trans_url = close_base_url + '/000/' + str(uuid.uuid4()) + '/websocket'
//        ws = WebSocket8Client(trans_url)
//        self.assertEqual(ws.recv(), u'o')
//        self.assertEqual(ws.recv(), u'c[3000,"Go away!"]')
//        with self.assertRaises(ws.ConnectionClosedException):
//            ws.recv()
//        ws.close()
//#
//Verify WebSocket headers sanity. Server must support both Hybi-07 and Hybi-10.
//
//    def test_headersSanity(self):
//        for version in ['7', '8', '13']:
//            url = base_url.split(':',1)[1] + \
//                '/000/' + str(uuid.uuid4()) + '/websocket'
//            ws_url = 'ws:' + url
//            http_url = 'http:' + url
//            origin = '/'.join(http_url.split('/')[:3])
//            h = {'Upgrade': 'websocket',
//                 'Connection': 'Upgrade',
//                 'Sec-WebSocket-Version': version,
//                 'Sec-WebSocket-Origin': 'http://asd',
//                 'Sec-WebSocket-Key': 'x3JJHMbDL1EzLkh9GBhXDw==',
//                 }
//
//            r = GET_async(http_url, headers=h)
//            self.assertEqual(r.status, 101)
//            self.assertEqual(r['sec-websocket-accept'], 'HSmrc0sMlYUkAGmm5OPpG2HaGWk=')
//            self.assertEqual(r['connection'].lower(), 'upgrade')
//            self.assertEqual(r['upgrade'].lower(), 'websocket')
//            self.assertFalse(r['content-length'])
//            r.close()
//#
//When user sends broken data - broken JSON for example, the server must abruptly terminate the ws connection.
//
//    def test_broken_json(self):
//        ws_url = 'ws:' + base_url.split(':',1)[1] + \
//                 '/000/' + str(uuid.uuid4()) + '/websocket'
//        ws = WebSocket8Client(ws_url)
//        self.assertEqual(ws.recv(), u'o')
//        ws.send(u'["a')
//        with self.assertRaises(ws.ConnectionClosedException):
//            ws.recv()
//        ws.close()
//#
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
}
