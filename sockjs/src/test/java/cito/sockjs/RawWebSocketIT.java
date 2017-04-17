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
 * @author Daniel Siviter
 * @since v1.0 [1 Mar 2017]
 */
public class RawWebSocketIT {
	/**
	 * Test the streaming transport.
	 */
	@Test
	@RunAsClient
	public void transport() throws IOException {
//        ws = WebSocket8Client(base_url + '/websocket')
//        ws.send(u'Hello world!\uffff')
//        self.assertEqual(ws.recv(), u'Hello world!\uffff')
//        ws.close()
	}
	
	/**
	 * Test the closing of transport.
	 */
	@Test
	@RunAsClient
	public void close() throws IOException {
//        ws = WebSocket8Client(close_base_url + '/websocket')
//        with self.assertRaises(ws.ConnectionClosedException):
//            ws.recv()
//        ws.close()
	}
}
