package cito.stomp.server;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Set;

import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
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
import cito.stomp.server.annotation.OnSubscribe;
import cito.stomp.server.event.MessageEvent;

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

		verifyNoMoreInteractions(beforeBeanDiscovery);
	}

	@Test
	public void registerFrameObserver() {
		final ProcessObserverMethod<MessageEvent, ?> processObserverMethod = mock(ProcessObserverMethod.class);

		this.extension.registerFrameObserver(OnSubscribe.class, processObserverMethod);

		verifyNoMoreInteractions(processObserverMethod);
	}

	@Test
	public void register() {
		final ProcessObserverMethod<MessageEvent, ?> processObserverMethod = mock(ProcessObserverMethod.class);

		this.extension.register(processObserverMethod, beanManager);

		verifyNoMoreInteractions(processObserverMethod);
	}

	@Test
	public void getObservers() {
		final Set<ObserverMethod<MessageEvent>> results = this.extension.getObservers(OnSubscribe.class);
	}

	@Test
	public void registerContexts() {
		final AfterBeanDiscovery afterBeanDiscovery = mock(AfterBeanDiscovery.class);
		this.extension.registerContexts(afterBeanDiscovery, this.beanManager);

		verifyNoMoreInteractions(afterBeanDiscovery);

	}

	@Test
	public void initialiseContexts() {
		final AfterDeploymentValidation afterDeploymentValidation = mock(AfterDeploymentValidation.class);
		this.extension.initialiseContexts(afterDeploymentValidation, this.beanManager);

		verifyNoMoreInteractions(afterDeploymentValidation);
	}

	@Test
	public void getWebSocketContext() {
		Extension.getWebSocketContext(this.beanManager);
	}

	@Test
	public void activateScope() {
		final Session session = mock(Session.class);

		final QuietClosable closable = Extension.activateScope(this.beanManager, session);

		verifyNoMoreInteractions(session);
	}

	@Test
	public void disposeScope() {
		final Session session = mock(Session.class);

		Extension.disposeScope(this.beanManager, session);

		verifyNoMoreInteractions(session);
	}

	@After
	public void after() {
		verifyNoMoreInteractions(this.beanManager);
	}
}
