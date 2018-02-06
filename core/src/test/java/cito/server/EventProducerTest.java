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
package cito.server;

import static cito.annotation.OnSend.Literal.onSend;
import static cito.annotation.OnSubscribe.Literal.onSubscribe;
import static cito.annotation.OnUnsubscribe.Literal.onUnsubscribe;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import cito.ReflectionUtil;
import cito.annotation.OnConnected;
import cito.annotation.OnDisconnect;
import cito.annotation.OnSend;
import cito.annotation.OnSubscribe;
import cito.annotation.OnUnsubscribe;
import cito.event.Message;
import cito.stomp.Command;
import cito.stomp.Frame;

/**
 * Unit test for {@link EventProducer}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Jul 2016]
 */
public class EventProducerTest {
	@Rule
	public MockitoRule mockito = MockitoJUnit.rule();

	@Mock
	private BeanManager beanManager;
	@Mock
	private Extension extension;
	@Mock
	private ObserverMethod<Message> observerMethod;
	
	@InjectMocks
	private EventProducer eventProducer;

	@Before
	public void before() {
		when(this.beanManager.getExtension(Extension.class)).thenReturn(this.extension);
	}

	@Test
	public void message_CONNECTED() {
		when(this.extension.getMessageObservers(OnConnected.class)).thenReturn(Collections.singleton(this.observerMethod));

		final Message event = new Message(
				Frame.connnected("1.2", "sessionId", "server").build());

		this.eventProducer.message(event);

		verify(this.beanManager).getExtension(Extension.class);
		verify(this.extension).getMessageObservers(OnConnected.class);
		verify(this.observerMethod).notify(event);
	}

	@Test
	public void message_SEND() {
		when(this.extension.getMessageObservers(OnSend.class)).thenReturn(Collections.singleton(this.observerMethod));
		when(observerMethod.getObservedQualifiers()).thenReturn(Collections.singleton(onSend("topic/*")));

		final Message event = new Message(
				Frame.send("topic/foo", MediaType.APPLICATION_JSON_TYPE, "{}").build());

		this.eventProducer.message(event);

		verify(this.beanManager).getExtension(Extension.class);
		verify(this.extension).getMessageObservers(OnSend.class);
		verify(this.observerMethod).getObservedQualifiers();
		verify(this.observerMethod).notify(event);
	}

	@Test
	public void message_SUBSCRIBE() {
		when(this.extension.getMessageObservers(OnSubscribe.class)).thenReturn(Collections.singleton(this.observerMethod));
		when(observerMethod.getObservedQualifiers()).thenReturn(Collections.singleton(onSubscribe("topic/*")));

		final Message event = new Message(
				Frame.builder(Command.SUBSCRIBE).destination("topic/foo").subscription("id").build());

		this.eventProducer.message(event);

		verify(this.beanManager).getExtension(Extension.class);
		verify(this.extension).getMessageObservers(OnSubscribe.class);
		verify(this.observerMethod).getObservedQualifiers();
		verify(this.observerMethod).notify(event);
		verifyNoMoreInteractions(this.observerMethod);
	}

	@Test
	public void message_UNSUBSCRIBE() {
		when(this.extension.getMessageObservers(OnUnsubscribe.class)).thenReturn(Collections.singleton(observerMethod));
		when(this.observerMethod.getObservedQualifiers()).thenReturn(Collections.singleton(onUnsubscribe("topic/*")));
		ReflectionUtil.<Map<String,String>>get(this.eventProducer, "idDestinationMap").put("id", "topic/foo");

		final Message event = new Message(
				Frame.builder(Command.UNSUBSCRIBE).subscription("id").build());

		this.eventProducer.message(event);

		verify(this.beanManager).getExtension(Extension.class);
		verify(this.extension).getMessageObservers(OnUnsubscribe.class);
		verify(this.observerMethod).getObservedQualifiers();
		verify(this.observerMethod).notify(event);
	}

	@Test
	public void message_DISCONNECT() {
		when(this.extension.getMessageObservers(OnDisconnect.class)).thenReturn(Collections.singleton(observerMethod));
	
		final Message event = new Message(
				Frame.builder(Command.DISCONNECT).build());

		this.eventProducer.message(event);

		verify(this.beanManager).getExtension(Extension.class);
		verify(this.extension).getMessageObservers(OnDisconnect.class);
		verify(this.observerMethod).notify(event);
		verifyNoMoreInteractions(this.observerMethod);
	}

	@After
	public void after() {
		verifyNoMoreInteractions(this.beanManager, this.extension, this.observerMethod);
	}
}
