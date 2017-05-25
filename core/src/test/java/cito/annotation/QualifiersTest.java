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

import static org.apache.deltaspike.core.util.metadata.AnnotationInstanceProvider.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Collections;

import org.junit.Test;

/**
 * @author Daniel Siviter
 * @since v1.0 [21 May 2017]
 */
public class QualifiersTest {
	@Test
	public void onConnected() {
		assertEquals(of(OnConnected.class), Qualifiers.onConnected());
		assertEquals(of(OnConnected.class).hashCode(), Qualifiers.onConnected().hashCode());
	}

	@Test
	public void onDisconnect() {
		assertEquals(of(OnDisconnect.class), Qualifiers.onDisconnect());
		assertEquals(of(OnDisconnect.class).hashCode(), Qualifiers.onDisconnect().hashCode());
	}

	@Test
	public void onSubscribe() {
		assertEquals(of(OnSubscribe.class), Qualifiers.onSubscribe(null));
		assertEquals(of(OnSubscribe.class).hashCode(), Qualifiers.onSubscribe(null).hashCode());

		assertEquals(of(OnSubscribe.class, Collections.singletonMap("value", "foo")), Qualifiers.onSubscribe("foo"));
		assertEquals(of(OnSubscribe.class, Collections.singletonMap("value", "foo")).hashCode(), Qualifiers.onSubscribe("foo").hashCode());

		assertNotEquals(of(OnSubscribe.class, Collections.singletonMap("value", "foo")), Qualifiers.onSubscribe("bar"));
		assertNotEquals(of(OnSubscribe.class, Collections.singletonMap("value", "foo")).hashCode(), Qualifiers.onSubscribe("bar").hashCode());
	}

	
	@Test
	public void onUnsubscribe() {
		assertEquals(of(OnUnsubscribe.class), Qualifiers.onUnsubscribe(null));
		assertEquals(of(OnUnsubscribe.class).hashCode(), Qualifiers.onUnsubscribe(null).hashCode());

		assertEquals(of(OnUnsubscribe.class, Collections.singletonMap("value", "foo")), Qualifiers.onUnsubscribe("foo"));
		assertEquals(of(OnUnsubscribe.class, Collections.singletonMap("value", "foo")).hashCode(), Qualifiers.onUnsubscribe("foo").hashCode());

		assertNotEquals(of(OnUnsubscribe.class, Collections.singletonMap("value", "foo")), Qualifiers.onUnsubscribe("bar"));
		assertNotEquals(of(OnUnsubscribe.class, Collections.singletonMap("value", "foo")).hashCode(), Qualifiers.onUnsubscribe("bar").hashCode());
	}

	@Test
	public void onSend() {
		assertEquals(of(OnSend.class), Qualifiers.onSend(null));
		assertEquals(of(OnSend.class).hashCode(), Qualifiers.onSend(null).hashCode());

		assertEquals(of(OnSend.class, Collections.singletonMap("value", "foo")), Qualifiers.onSend("foo"));
		assertEquals(of(OnSend.class, Collections.singletonMap("value", "foo")).hashCode(), Qualifiers.onSend("foo").hashCode());

		assertNotEquals(of(OnSend.class, Collections.singletonMap("value", "foo")), Qualifiers.onSend("bar"));
		assertNotEquals(of(OnSend.class, Collections.singletonMap("value", "foo")).hashCode(), Qualifiers.onSend("bar").hashCode());
	}
}
