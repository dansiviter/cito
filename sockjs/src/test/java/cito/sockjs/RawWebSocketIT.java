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
import static org.junit.Assert.assertNull;
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
 * @author Daniel Siviter
 * @since v1.0 [1 Mar 2017]
 */
public class RawWebSocketIT extends AbstractWebSocketIT {
	/**
	 * Test the streaming transport.
	 * @throws DeploymentException 
	 */
	@Test
	@RunAsClient
	public void transport() throws IOException, DeploymentException {
		final URI path = uri(EndpointType.ECHO).path("websocket").build();
		final ClientEndpointConfig config = ClientEndpointConfig.Builder.create().build();

		final ClientEndpoint endpoint = new ClientEndpoint();
		try (Session session = this.container.connectToServer(endpoint, config, path)) {
			session.getBasicRemote().sendText("Hello world!\uffff");
			assertEquals("Hello world!\uffff", endpoint.get());
		}
	}
	
	/**
	 * Test the closing of transport.
	 * @throws DeploymentException 
	 */
	@Test
	@RunAsClient
	public void close() throws IOException, DeploymentException {
		final URI path = uri(EndpointType.CLOSE).path("websocket").build();
		final ClientEndpointConfig config = ClientEndpointConfig.Builder.create().build();

		final ClientEndpoint endpoint = new ClientEndpoint();
		try (Session session = this.container.connectToServer(endpoint, config, path)) {
			assertNull(endpoint.get());
			assertTrue(endpoint.wasClosed());
		}
	}


	// --- Static Methods ---

	@Deployment
	public static WebArchive createDeployment() {
		return createWebArchive();
	}
}
