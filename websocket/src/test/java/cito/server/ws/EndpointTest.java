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
package cito.server.ws;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.junit.Test;

import cito.ReflectionUtil;
import cito.server.AbstractEndpoint;
import cito.stomp.Frame;

/**
 * Unit tests for {@link Endpoint}. This doesn't add much over {@link AbstractEndpoint} so just testing the annotations.
 * 
 * @author Daniel Siviter
 * @since v1.0 [17 Apr 2017]
 */
public class EndpointTest {

	@Test
	public void onOpen() {
		final Method method = ReflectionUtil.findMethod(Endpoint.class, "onOpen", Session.class, EndpointConfig.class);
		assertNotNull(method.getAnnotation(OnOpen.class));
	}

	@Test
	public void message() {
		final Method method = ReflectionUtil.findMethod(Endpoint.class, "message", Session.class, Frame.class);
		assertNotNull(method.getAnnotation(OnMessage.class));
	}

	@Test
	public void onClose() {
		final Method method = ReflectionUtil.findMethod(Endpoint.class, "onClose", Session.class, CloseReason.class);
		assertNotNull(method.getAnnotation(OnClose.class));
	}

	@Test
	public void onError() {
		final Method method = ReflectionUtil.findMethod(Endpoint.class, "onError", Session.class, Throwable.class);
		assertNotNull(method.getAnnotation(OnError.class));
	}
}
