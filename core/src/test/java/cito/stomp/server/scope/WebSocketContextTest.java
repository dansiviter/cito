package cito.stomp.server.scope;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.spi.BeanManager;
import javax.websocket.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import cito.QuietClosable;
import cito.stomp.server.annotation.WebSocketScope;

/**
 * Unit test for {@link WebSocketContext}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [22 Nov 2016]
 */
@RunWith(MockitoJUnitRunner.class)
@Ignore
public class WebSocketContextTest {
	@Mock
	private BeanManager beanManager;
	@Mock
	private WebSocketSessionHolder sessionHolder;
	@Mock
	private Session session;

	private WebSocketContext context;

	@Before
	public void before() {
		when(this.session.getId()).thenReturn("sessionId");

		this.context = new WebSocketContext(this.beanManager);
		this.context.init(this.sessionHolder);
	}

	@Test
	public void activate() {
		final QuietClosable closable = this.context.activate(session);
		closable.close();

		verify(this.session).getId();
		verify(this.sessionHolder).set(this.session);
		verify(this.sessionHolder).remove();
	}

	@Test
	public void dispose() {
		this.context.dispose(session);

		verify(this.session).getId();
		verify(this.sessionHolder).set(this.session);
		verify(this.sessionHolder).remove();
	}

	@After
	public void after() {
		verify(beanManager).isPassivatingScope(WebSocketScope.class);
		verifyNoMoreInteractions(this.beanManager, this.sessionHolder, this.session);
	}
}
