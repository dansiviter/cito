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
package cito.annotation;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.apache.deltaspike.core.util.metadata.AnnotationInstanceProvider.of;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [19 Jul 2016]
 */
public enum Qualifiers { ;
	private static final OnConnected ON_CONNECTED = of(OnConnected.class);
	private static final OnDisconnect ON_DISCONNECT = of(OnDisconnect.class);
	private static final OnOpen ON_OPEN = of(OnOpen.class);
	private static final OnError ON_ERROR = of(OnError.class);
	private static final OnClose ON_CLOSE = of(OnClose.class);
	private static final FromBroker FROM_BROKER = of(FromBroker.class);
	private static final FromClient FROM_CLIENT = of(FromClient.class);
	private static final FromServer FROM_SERVER = of(FromServer.class);

	/**
	 * 
	 * @return
	 */
	public static OnConnected onConnected() {
		return ON_CONNECTED;
	}

	/**
	 * 
	 * @return
	 */
	public static OnDisconnect onDisconnect() {
		return ON_DISCONNECT;
	}

	/**
	 * 
	 * @param value
	 * @return
	 */
	public static OnSubscribe onSubscribe(String value) {
		return of(OnSubscribe.class, value == null ? emptyMap() : singletonMap("value", value));
	}

	/**
	 * 
	 * @param value
	 * @return
	 */
	public static OnUnsubscribe onUnsubscribe(String value) {
		return of(OnUnsubscribe.class, value == null ? emptyMap() : singletonMap("value", value));
	}

	/**
	 * 
	 * @param value
	 * @return
	 */
	public static OnSend onSend(String value) {
		return of(OnSend.class, value == null ? emptyMap() : singletonMap("value", value));
	}

	/**
	 * 
	 * @return
	 */
	public static OnOpen onOpen() {
		return ON_OPEN;
	}

	/**
	 * 
	 * @return
	 */
	public static OnError onError() {
		return ON_ERROR;
	}

	/**
	 * 
	 * @return
	 */
	public static OnClose onClose() {
		return ON_CLOSE;
	}

	/**
	 * 
	 * @return
	 */
	public static FromBroker fromBroker() {
		return FROM_BROKER;
	}

	/**
	 * 
	 * @return
	 */
	public static FromClient fromClient() {
		return FROM_CLIENT;
	}

	/**
	 * 
	 * @return
	 */
	public static FromServer fromServer() {
		return FROM_SERVER;
	}
}

