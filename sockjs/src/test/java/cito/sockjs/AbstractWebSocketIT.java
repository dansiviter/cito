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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.junit.Before;

/**
 * Abstract integration test for checking WebSocket connections.
 * 
 * @author Daniel Siviter
 * @since v1.0 [8 Jul 2017]
 */
public abstract class AbstractWebSocketIT extends AbstractIT {
	protected WebSocketContainer container;

	@Before
	public void before() {
		this.container = ContainerProvider.getWebSocketContainer();
	}


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [6 Jul 2017]
	 */
	protected class ClientEndpoint extends Endpoint implements MessageHandler.Whole<String>{
		private final BlockingQueue<String> queue = new ArrayBlockingQueue<>(5, true);
		private final CountDownLatch closeLatch = new CountDownLatch(1);

		@Override
		public void onOpen(Session session, EndpointConfig config) {
			session.addMessageHandler(this);
		}

		@Override
		public void onMessage(String message) {
			queue.add(message);
		}

		public String get() {
			try {
				return this.queue.poll(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
		}

		@Override
		public void onClose(Session session, CloseReason closeReason) {
			closeLatch.countDown();
		}

		public boolean wasClosed() {
			try {
				return closeLatch.await(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
		}
	}
}
