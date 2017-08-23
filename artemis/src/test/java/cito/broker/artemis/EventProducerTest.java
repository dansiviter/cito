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
package cito.broker.artemis;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
import static org.apache.activemq.artemis.api.core.management.ManagementHelper.HDR_NOTIFICATION_TYPE;
import static org.apache.activemq.artemis.jms.server.management.JMSNotificationType.MESSAGE;
import static org.apache.deltaspike.core.util.metadata.AnnotationInstanceProvider.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.management.CoreNotificationType;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;
import org.apache.activemq.artemis.jms.server.management.JMSNotificationType;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import cito.ReflectionUtil;
import cito.annotation.OnAdded;
import cito.annotation.OnRemoved;
import cito.event.DestinationChanged;
import cito.event.DestinationChanged.Type;
import cito.server.Extension;


/**
 * Unit test for {@link EventProducer}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [17 Jan 2017]
 */
@RunWith(MockitoJUnitRunner.class)
public class EventProducerTest {
	@Mock
	private Logger log;
	@Mock
	private Instance<JMSContext> jmsCtxProvider;
	@Mock
	private BeanManager beanManager;
	@Mock
	private Configuration artemisConfig;
	@Mock
	private EmbeddedJMS broker;
	@Mock
	private Message message;

	@InjectMocks
	private EventProducer eventProducer;

	@Test
	public void startup() {
		connectInternal(() -> this.eventProducer.startup(new Object()));
	}

	@Test
	public void connect() {
		connectInternal(this.eventProducer::connect);
	}

	private void connectInternal(Runnable command) {
		final JMSContext jmsCtx = mock(JMSContext.class);
		when(this.jmsCtxProvider.get()).thenReturn(jmsCtx);

		when(this.artemisConfig.getManagementNotificationAddress()).thenReturn(new SimpleString("notif"));
		final JMSConsumer consumer = mock(JMSConsumer.class);
		when(jmsCtx.createConsumer(any())).thenReturn(consumer);

		command.run();

		verify(this.jmsCtxProvider).get();
		verify(this.artemisConfig).getManagementNotificationAddress();
		verify(this.log).info("Connecting to broker for sourcing destination events.");
		verify(jmsCtx).createConsumer(any());
		verify(consumer).setMessageListener(this.eventProducer);
		verifyNoMoreInteractions(consumer);
	}

	@Test
	public void onMessage_unsupported() throws JMSException {
		for (CoreNotificationType type : CoreNotificationType.values()) {
			when(this.message.getStringProperty(HDR_NOTIFICATION_TYPE.toString())).thenReturn(type.name());
			this.eventProducer.onMessage(message);
		}
		when(this.message.getStringProperty(HDR_NOTIFICATION_TYPE.toString())).thenReturn(JMSNotificationType.CONNECTION_FACTORY_CREATED.name());
		this.eventProducer.onMessage(message);
		when(this.message.getStringProperty(HDR_NOTIFICATION_TYPE.toString())).thenReturn(JMSNotificationType.CONNECTION_FACTORY_DESTROYED.name());
		this.eventProducer.onMessage(message);

		verify(this.message, times(22)).getStringProperty(HDR_NOTIFICATION_TYPE.toString());
	}

	@Test
	public void onMessage_unknown() throws JMSException {
		when(this.message.getStringProperty(HDR_NOTIFICATION_TYPE.toString())).thenReturn("FOO");

		this.eventProducer.onMessage(message);

		verify(this.message).getStringProperty(HDR_NOTIFICATION_TYPE.toString());
		verify(this.log).error(eq("Unable to process notification!"), any(Exception.class));
	}

	@Test
	public void onMessage_onAdded() throws JMSException {
		onMessage(OnAdded.class, JMSNotificationType.TOPIC_CREATED, Type.ADDED);
	}

	@Test
	public void message_onRemoved() throws JMSException {
		onMessage(OnRemoved.class, JMSNotificationType.TOPIC_DESTROYED, Type.REMOVED);
	}

	private void onMessage(Class<? extends Annotation> cls, JMSNotificationType notificationType, Type type) throws JMSException {
		when(this.message.getStringProperty(MESSAGE.toString())).thenReturn("foo");
		final Extension extension = mock(Extension.class);
		when(this.beanManager.getExtension(Extension.class)).thenReturn(extension);
		@SuppressWarnings("unchecked")
		final ObserverMethod<DestinationChanged> observerMethod = mock(ObserverMethod.class);
		when(extension.getDestinationObservers(cls)).thenReturn(singleton(observerMethod));
		when(observerMethod.getObservedQualifiers()).thenReturn(singleton(of(cls)));

		when(this.message.getStringProperty(HDR_NOTIFICATION_TYPE.toString())).thenReturn(notificationType.name());
		this.eventProducer.onMessage(message);

		verify(this.message).getStringProperty(MESSAGE.toString());
		verify(this.message).getStringProperty(HDR_NOTIFICATION_TYPE.toString());
		verify(this.log).info(eq("Destination changed. [type={},destination={}]"), eq(type), anyString());
		verify(this.beanManager).getExtension(Extension.class);
		verify(extension).getDestinationObservers(cls);
		verify(observerMethod).getObservedQualifiers();
		verify(observerMethod).notify(any());
		verifyNoMoreInteractions(extension, observerMethod);
	}

	@Test
	public void onMessage_noMatch() throws JMSException {
		when(this.message.getStringProperty(MESSAGE.toString())).thenReturn("foo");
		final Extension extension = mock(Extension.class);
		when(this.beanManager.getExtension(Extension.class)).thenReturn(extension);
		@SuppressWarnings("unchecked")
		final ObserverMethod<DestinationChanged> observerMethod = mock(ObserverMethod.class);
		when(extension.getDestinationObservers(OnAdded.class)).thenReturn(singleton(observerMethod));
		when(observerMethod.getObservedQualifiers()).thenReturn(singleton(of(OnAdded.class, singletonMap("value", "acme"))));

		when(this.message.getStringProperty(HDR_NOTIFICATION_TYPE.toString())).thenReturn(JMSNotificationType.TOPIC_CREATED.name());
		this.eventProducer.onMessage(message);

		verify(this.message).getStringProperty(MESSAGE.toString());
		verify(this.message).getStringProperty(HDR_NOTIFICATION_TYPE.toString());
		verify(this.log).info(eq("Destination changed. [type={},destination={}]"), eq(Type.ADDED), anyString());
		verify(this.beanManager).getExtension(Extension.class);
		verify(extension).getDestinationObservers(OnAdded.class);
		verify(observerMethod).getObservedQualifiers();
		verifyNoMoreInteractions(extension, observerMethod);
	}

	@Test
	public void destroy() {
		final JMSConsumer consumer = mock(JMSConsumer.class);
		ReflectionUtil.set(this.eventProducer, "consumer", consumer);

		this.eventProducer.destroy();

		verify(consumer).close();
		verifyNoMoreInteractions(consumer);
	}

	@Test
	public void destroy_noComsumer() {
		this.eventProducer.destroy();
	}

	@After
	public void after() {
		verifyNoMoreInteractions(
				this.log,
				this.jmsCtxProvider,
				this.beanManager,
				this.artemisConfig,
				this.broker,
				this.message);
	}
}
