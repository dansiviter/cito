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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProcessObserverMethod;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import cito.ReflectionUtil;
import cito.annotation.OnConnected;
import cito.annotation.OnDisconnect;
import cito.annotation.OnSend;
import cito.annotation.OnSubscribe;
import cito.annotation.OnUnsubscribe;
import cito.annotation.Qualifiers;
import cito.annotation.WebSocketScope;
import cito.event.Message;
import cito.scope.WebSocketContext;
import cito.scope.WebSocketSessionHolder;

/**
 * Unit test for {@link Extension}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Jul 2016]
 */
@RunWith(MockitoJUnitRunner.class)
public class ExtensionTest {
	@Mock
	private BeanManager beanManager;

	private Extension extension;

	@Before
	public void before() {
		this.extension = new Extension();
	}

	@Test
	public void addScope() {
		final BeforeBeanDiscovery beforeBeanDiscovery = mock(BeforeBeanDiscovery.class);

		this.extension.addScope(beforeBeanDiscovery);

		verify(beforeBeanDiscovery).addScope(WebSocketScope.class, true, false);
		verifyNoMoreInteractions(beforeBeanDiscovery);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void register_onConnected() {
		final ProcessObserverMethod<Message, ?> processObserverMethod = mock(ProcessObserverMethod.class);
		final ObserverMethod<Message> observerMethod = mock(ObserverMethod.class);
		when(processObserverMethod.getObserverMethod()).thenReturn(observerMethod);
		when(observerMethod.getObservedQualifiers()).thenReturn(Collections.singleton(Qualifiers.onConnected()));

		this.extension.registerMessageEvent(processObserverMethod);

		assertEquals(observerMethod, getMessageObservers(this.extension).get(OnConnected.class).iterator().next());

		verify(processObserverMethod).getObserverMethod();
		verify(observerMethod).getObservedQualifiers();
		verifyNoMoreInteractions(processObserverMethod, observerMethod);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void register_onSend() {
		final ProcessObserverMethod<Message, ?> processObserverMethod = mock(ProcessObserverMethod.class);
		final ObserverMethod<Message> observerMethod = mock(ObserverMethod.class);
		when(processObserverMethod.getObserverMethod()).thenReturn(observerMethod);
		when(observerMethod.getObservedQualifiers()).thenReturn(Collections.singleton(Qualifiers.onSend("")));

		this.extension.registerMessageEvent(processObserverMethod);

		assertEquals(observerMethod, getMessageObservers(this.extension).get(OnSend.class).iterator().next());

		verify(processObserverMethod).getObserverMethod();
		verify(observerMethod).getObservedQualifiers();
		verifyNoMoreInteractions(processObserverMethod, observerMethod);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void register_onSubscribe() {
		final ProcessObserverMethod<Message, ?> processObserverMethod = mock(ProcessObserverMethod.class);
		final ObserverMethod<Message> observerMethod = mock(ObserverMethod.class);
		when(processObserverMethod.getObserverMethod()).thenReturn(observerMethod);
		when(observerMethod.getObservedQualifiers()).thenReturn(Collections.singleton(Qualifiers.onSubscribe("")));

		this.extension.registerMessageEvent(processObserverMethod);

		assertEquals(observerMethod, getMessageObservers(this.extension).get(OnSubscribe.class).iterator().next());

		verify(processObserverMethod).getObserverMethod();
		verify(observerMethod).getObservedQualifiers();
		verifyNoMoreInteractions(processObserverMethod, observerMethod);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void register_onUnsubscribe() {
		final ProcessObserverMethod<Message, ?> processObserverMethod = mock(ProcessObserverMethod.class);
		final ObserverMethod<Message> observerMethod = mock(ObserverMethod.class);
		when(processObserverMethod.getObserverMethod()).thenReturn(observerMethod);
		when(observerMethod.getObservedQualifiers()).thenReturn(Collections.singleton(Qualifiers.onUnsubscribe("")));

		this.extension.registerMessageEvent(processObserverMethod);

		assertEquals(observerMethod, getMessageObservers(this.extension).get(OnUnsubscribe.class).iterator().next());

		verify(processObserverMethod).getObserverMethod();
		verify(observerMethod).getObservedQualifiers();
		verifyNoMoreInteractions(processObserverMethod, observerMethod);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void register_onDisconnect() {
		final ProcessObserverMethod<Message, ?> processObserverMethod = mock(ProcessObserverMethod.class);
		final ObserverMethod<Message> observerMethod = mock(ObserverMethod.class);
		when(processObserverMethod.getObserverMethod()).thenReturn(observerMethod);
		when(observerMethod.getObservedQualifiers()).thenReturn(Collections.singleton(Qualifiers.onDisconnect()));

		this.extension.registerMessageEvent(processObserverMethod);

		assertEquals(observerMethod, getMessageObservers(this.extension).get(OnDisconnect.class).iterator().next());

		verify(processObserverMethod).getObserverMethod();
		verify(observerMethod).getObservedQualifiers();
		verifyNoMoreInteractions(processObserverMethod, observerMethod);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getObservers() {
		final ObserverMethod<Message> observerMethod = mock(ObserverMethod.class);
		getMessageObservers(this.extension).put(OnSubscribe.class, Collections.singleton(observerMethod));

		final Set<ObserverMethod<Message>> results = this.extension.getMessageObservers(OnSubscribe.class);

		assertEquals(Collections.singleton(observerMethod), results);

		verifyNoMoreInteractions(observerMethod);
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void registerContexts() {
		final AfterBeanDiscovery afterBeanDiscovery = mock(AfterBeanDiscovery.class);
		final AnnotatedType annotatedType = mock(AnnotatedType.class);
		when(this.beanManager.createAnnotatedType(any())).thenReturn(annotatedType);

		this.extension.registerContexts(afterBeanDiscovery, this.beanManager);

		verify(afterBeanDiscovery).addContext(any(WebSocketContext.class));
		verify(this.beanManager).isPassivatingScope(WebSocketScope.class);
		verifyNoMoreInteractions(afterBeanDiscovery);
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void initialiseContexts() {
		final WebSocketContext webSocketContext = mock(WebSocketContext.class);
		ReflectionUtil.set(this.extension, "webSocketContext", webSocketContext);
		final AfterDeploymentValidation afterDeploymentValidation = mock(AfterDeploymentValidation.class);
		final Bean<?> bean = mock(Bean.class);
		when(this.beanManager.getBeans(WebSocketSessionHolder.class)).thenReturn(Collections.singleton(bean));
		when(this.beanManager.resolve(any(Set.class))).thenReturn(bean);
		final CreationalContext creationalContext = mock(CreationalContext.class);
		when(this.beanManager.createCreationalContext(bean)).thenReturn(creationalContext);
		final WebSocketSessionHolder webSocketSessionHolder = mock(WebSocketSessionHolder.class);
		when(this.beanManager.getReference(bean, WebSocketSessionHolder.class, creationalContext)).thenReturn(webSocketSessionHolder);

		this.extension.initialiseContexts(afterDeploymentValidation, this.beanManager);

		verify(this.beanManager).getBeans(WebSocketSessionHolder.class);
		verify(this.beanManager).resolve(any(Set.class));
		verify(this.beanManager).createCreationalContext(bean);
		verify(this.beanManager).getReference(bean, WebSocketSessionHolder.class, creationalContext);
		verify(webSocketContext).init(webSocketSessionHolder);
		verifyNoMoreInteractions(webSocketContext, afterDeploymentValidation, bean, creationalContext, webSocketSessionHolder);
	}

	@After
	public void after() {
		verifyNoMoreInteractions(this.beanManager);
	}


	// --- Static Method ---

	/**
	 * 
	 * @param e
	 * @return
	 */
	private static Map<Class<? extends Annotation>, Set<ObserverMethod<Message>>> getMessageObservers(Extension e) {
		return ReflectionUtil.get(e, "messageObservers");
	}
}
