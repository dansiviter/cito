package cito.stomp.server;


import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;


import static org.junit.Assert.fail;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import javax.enterprise.inject.spi.BeanManager;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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

	@InjectMocks
	private EventProducer eventProducer;

	@Test
	public void message_CONNECTED() {
		final MessageEvent event = mock(MessageEvent.class);

		this.eventProducer.message(event);

		verifyNoMoreInteractions(event);
	}

	@Test
	public void message_MESSAGE() {
		final MessageEvent event = mock(MessageEvent.class);

		this.eventProducer.message(event);

		verifyNoMoreInteractions(event);
	}

	@Test
	public void message_SUBSCRIBE() {
		final MessageEvent event = mock(MessageEvent.class);

		this.eventProducer.message(event);

		verifyNoMoreInteractions(event);
	}

	@Test
	public void message_UNSUBSCRIBE() {
		final MessageEvent event = mock(MessageEvent.class);

		this.eventProducer.message(event);

		verifyNoMoreInteractions(event);
	}

	@Test
	public void message_DISCONNECT() {
		final MessageEvent event = mock(MessageEvent.class);

		this.eventProducer.message(event);

		verifyNoMoreInteractions(event);
	}

	@After
	public void after() {
		verifyNoMoreInteractions(this.beanManager);
	}
}
