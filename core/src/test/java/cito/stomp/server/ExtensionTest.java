package cito.stomp.server;

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
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProcessObserverMethod;
import javax.websocket.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import cito.QuietClosable;
import cito.ReflectionUtil;
import cito.stomp.server.annotation.OnConnected;
import cito.stomp.server.annotation.OnDisconnect;
import cito.stomp.server.annotation.OnMessage;
import cito.stomp.server.annotation.OnSubscribe;
import cito.stomp.server.annotation.OnUnsubscribe;
import cito.stomp.server.annotation.Qualifiers;
import cito.stomp.server.annotation.WebSocketScope;
import cito.stomp.server.event.MessageEvent;
import cito.stomp.server.scope.WebSocketContext;
import cito.stomp.server.scope.WebSocketSessionHolder;

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
		final ProcessObserverMethod<MessageEvent, ?> processObserverMethod = mock(ProcessObserverMethod.class);
		final ObserverMethod<MessageEvent> observerMethod = mock(ObserverMethod.class);
		when(processObserverMethod.getObserverMethod()).thenReturn(observerMethod);
		when(observerMethod.getObservedQualifiers()).thenReturn(Collections.singleton(Qualifiers.onConnected()));

		this.extension.register(processObserverMethod, beanManager);

		assertEquals(observerMethod, getFrameObservers(this.extension).get(OnConnected.class).iterator().next());

		verify(processObserverMethod).getObserverMethod();
		verify(observerMethod).getObservedQualifiers();
		verifyNoMoreInteractions(processObserverMethod, observerMethod);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void register_onMessage() {
		final ProcessObserverMethod<MessageEvent, ?> processObserverMethod = mock(ProcessObserverMethod.class);
		final ObserverMethod<MessageEvent> observerMethod = mock(ObserverMethod.class);
		when(processObserverMethod.getObserverMethod()).thenReturn(observerMethod);
		when(observerMethod.getObservedQualifiers()).thenReturn(Collections.singleton(Qualifiers.onMessage("")));

		this.extension.register(processObserverMethod, beanManager);

		assertEquals(observerMethod, getFrameObservers(this.extension).get(OnMessage.class).iterator().next());

		verify(processObserverMethod).getObserverMethod();
		verify(observerMethod).getObservedQualifiers();
		verifyNoMoreInteractions(processObserverMethod, observerMethod);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void register_onSubscribe() {
		final ProcessObserverMethod<MessageEvent, ?> processObserverMethod = mock(ProcessObserverMethod.class);
		final ObserverMethod<MessageEvent> observerMethod = mock(ObserverMethod.class);
		when(processObserverMethod.getObserverMethod()).thenReturn(observerMethod);
		when(observerMethod.getObservedQualifiers()).thenReturn(Collections.singleton(Qualifiers.onSubscribe("")));

		this.extension.register(processObserverMethod, beanManager);

		assertEquals(observerMethod, getFrameObservers(this.extension).get(OnSubscribe.class).iterator().next());

		verify(processObserverMethod).getObserverMethod();
		verify(observerMethod).getObservedQualifiers();
		verifyNoMoreInteractions(processObserverMethod, observerMethod);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void register_onUnsubscribe() {
		final ProcessObserverMethod<MessageEvent, ?> processObserverMethod = mock(ProcessObserverMethod.class);
		final ObserverMethod<MessageEvent> observerMethod = mock(ObserverMethod.class);
		when(processObserverMethod.getObserverMethod()).thenReturn(observerMethod);
		when(observerMethod.getObservedQualifiers()).thenReturn(Collections.singleton(Qualifiers.onUnsubscribe("")));

		this.extension.register(processObserverMethod, beanManager);

		assertEquals(observerMethod, getFrameObservers(this.extension).get(OnUnsubscribe.class).iterator().next());

		verify(processObserverMethod).getObserverMethod();
		verify(observerMethod).getObservedQualifiers();
		verifyNoMoreInteractions(processObserverMethod, observerMethod);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void register_onDisconnect() {
		final ProcessObserverMethod<MessageEvent, ?> processObserverMethod = mock(ProcessObserverMethod.class);
		final ObserverMethod<MessageEvent> observerMethod = mock(ObserverMethod.class);
		when(processObserverMethod.getObserverMethod()).thenReturn(observerMethod);
		when(observerMethod.getObservedQualifiers()).thenReturn(Collections.singleton(Qualifiers.onDisconnect()));

		this.extension.register(processObserverMethod, beanManager);

		assertEquals(observerMethod, getFrameObservers(this.extension).get(OnDisconnect.class).iterator().next());

		verify(processObserverMethod).getObserverMethod();
		verify(observerMethod).getObservedQualifiers();
		verifyNoMoreInteractions(processObserverMethod, observerMethod);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getObservers() {
		final ObserverMethod<MessageEvent> observerMethod = mock(ObserverMethod.class);
		getFrameObservers(this.extension).put(OnSubscribe.class, Collections.singleton(observerMethod));

		final Set<ObserverMethod<MessageEvent>> results = this.extension.getObservers(OnSubscribe.class);

		assertEquals(Collections.singleton(observerMethod), results);

		verifyNoMoreInteractions(observerMethod);
	}

	@Test
	public void registerContexts() {
		final AfterBeanDiscovery afterBeanDiscovery = mock(AfterBeanDiscovery.class);
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

	@SuppressWarnings("unused")
	@Test
	public void activateScope() {
		final WebSocketContext webSocketContext = mock(WebSocketContext.class);
		ReflectionUtil.set(this.extension, "webSocketContext", webSocketContext);
		when(this.beanManager.getExtension(Extension.class)).thenReturn(this.extension);
		final Session session = mock(Session.class);

		final QuietClosable closable = Extension.activateScope(this.beanManager, session);

		verify(this.beanManager).getExtension(Extension.class);
		verify(webSocketContext).activate(session);
		verifyNoMoreInteractions(webSocketContext, session);
	}

	@Test
	public void disposeScope() {
		final WebSocketContext webSocketContext = mock(WebSocketContext.class);
		ReflectionUtil.set(this.extension, "webSocketContext", webSocketContext);
		when(this.beanManager.getExtension(Extension.class)).thenReturn(this.extension);
		final Session session = mock(Session.class);

		Extension.disposeScope(this.beanManager, session);

		verify(this.beanManager).getExtension(Extension.class);
		verify(webSocketContext).dispose(session);
		verifyNoMoreInteractions(webSocketContext, session);
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
	private static Map<Class<? extends Annotation>, Set<ObserverMethod<MessageEvent>>> getFrameObservers(Extension e) {
		return ReflectionUtil.get(e, "frameObservers");
	}
}
