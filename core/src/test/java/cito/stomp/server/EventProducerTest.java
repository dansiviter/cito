package cito.stomp.server;

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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import cito.ReflectionUtil;
import cito.stomp.Command;
import cito.stomp.Frame;
import cito.stomp.server.annotation.OnConnected;
import cito.stomp.server.annotation.OnDisconnect;
import cito.stomp.server.annotation.OnSend;
import cito.stomp.server.annotation.OnSubscribe;
import cito.stomp.server.annotation.OnUnsubscribe;
import cito.stomp.server.annotation.Qualifiers;
import cito.stomp.server.event.BasicMessageEvent;
import cito.stomp.server.event.MessageEvent;

/**
 * Unit test for {@link EventProducer}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Jul 2016]
 */
@RunWith(MockitoJUnitRunner.class)
public class EventProducerTest {
	@Mock
	private BeanManager beanManager;
	@Mock
	private Extension extension;
	@Mock
	private ObserverMethod<MessageEvent> observerMethod;
	
	@InjectMocks
	private EventProducer eventProducer;

	@Before
	public void before() {
		when(this.beanManager.getExtension(Extension.class)).thenReturn(this.extension);
	}

	@Test
	public void message_CONNECTED() {
		when(this.extension.getObservers(OnConnected.class)).thenReturn(Collections.singleton(this.observerMethod));

		final MessageEvent event = new BasicMessageEvent(
				Frame.connnected("1.2", "sessionId", "server").build());

		this.eventProducer.message(event);

		verify(this.beanManager).getExtension(Extension.class);
		verify(this.extension).getObservers(OnConnected.class);
		verify(this.observerMethod).notify(event);
	}

	@Test
	public void message_SEND() {;
		when(this.extension.getObservers(OnSend.class)).thenReturn(Collections.singleton(this.observerMethod));
		when(observerMethod.getObservedQualifiers()).thenReturn(Collections.singleton(Qualifiers.onSend("/topic/*")));

		final MessageEvent event = new BasicMessageEvent(
				Frame.send("/topic/foo", MediaType.APPLICATION_JSON_TYPE, "{}").build());

		this.eventProducer.message(event);

		verify(this.beanManager).getExtension(Extension.class);
		verify(this.extension).getObservers(OnSend.class);
		verify(this.observerMethod).getObservedQualifiers();
		verify(this.observerMethod).notify(event);
	}

	@Test
	public void message_SUBSCRIBE() {
		when(this.extension.getObservers(OnSubscribe.class)).thenReturn(Collections.singleton(this.observerMethod));
		when(observerMethod.getObservedQualifiers()).thenReturn(Collections.singleton(Qualifiers.onSubscribe("/topic/*")));

		final MessageEvent event = new BasicMessageEvent(
				Frame.builder(Command.SUBSCRIBE).destination("/topic/foo").subscription("id").build());

		this.eventProducer.message(event);

		verify(this.beanManager).getExtension(Extension.class);
		verify(this.extension).getObservers(OnSubscribe.class);
		verify(this.observerMethod).getObservedQualifiers();
		verify(this.observerMethod).notify(event);
		verifyNoMoreInteractions(this.observerMethod);
	}

	@Test
	public void message_UNSUBSCRIBE() {
		when(this.extension.getObservers(OnUnsubscribe.class)).thenReturn(Collections.singleton(observerMethod));
		when(this.observerMethod.getObservedQualifiers()).thenReturn(Collections.singleton(Qualifiers.onUnsubscribe("/topic/*")));
		ReflectionUtil.<Map<String,String>>get(this.eventProducer, "idDestinationMap").put("id", "/topic/foo");

		final MessageEvent event = new BasicMessageEvent(
				Frame.builder(Command.UNSUBSCRIBE).subscription("id").build());

		this.eventProducer.message(event);

		verify(this.beanManager).getExtension(Extension.class);
		verify(this.extension).getObservers(OnUnsubscribe.class);
		verify(this.observerMethod).getObservedQualifiers();
		verify(this.observerMethod).notify(event);
	}

	@Test
	public void message_DISCONNECT() {
		when(this.extension.getObservers(OnDisconnect.class)).thenReturn(Collections.singleton(observerMethod));
		when(observerMethod.getObservedQualifiers()).thenReturn(Collections.singleton(Qualifiers.onSubscribe("/topic/*")));

		final MessageEvent event = new BasicMessageEvent(
				Frame.builder(Command.DISCONNECT).build());

		this.eventProducer.message(event);

		verify(this.beanManager).getExtension(Extension.class);
		verify(this.extension).getObservers(OnDisconnect.class);
		verify(this.observerMethod).notify(event);
		verifyNoMoreInteractions(this.observerMethod);
	}

	@After
	public void after() {
		verifyNoMoreInteractions(this.beanManager, this.extension, this.observerMethod);
	}
}
