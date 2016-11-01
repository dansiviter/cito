package cito.stomp.jms;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import javax.jms.JMSException;
import javax.jms.Message;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import cito.stomp.Frame;

/**
 * Unit test for {@link Subscription}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Jul 2016]
 */
@RunWith(MockitoJUnitRunner.class)
public class SubscriptionTest {
	@Mock
	private Session session;
	@Mock
	private Frame frame;
	@Mock
	private Factory factory;

	private Subscription subscription;

	@Before
	public void before() throws JMSException {
		this.subscription = new Subscription(this.session, "id", frame, this.factory);
	}

	@Test
	public void getDestination() {
		this.subscription.getDestination();
	}

	@Test
	public void onMessage() {
		final Message message = mock(Message.class);

		this.subscription.onMessage(message);

		verifyNoMoreInteractions(message);
	}

	@Test
	public void close() throws JMSException {
		this.subscription.close();
	}

	@After
	public void after() {
		verifyNoMoreInteractions(this.session, this.frame, this.factory);
	}
}
