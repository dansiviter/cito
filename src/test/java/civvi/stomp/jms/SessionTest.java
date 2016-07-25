package civvi.stomp.jms;

import static org.mockito.Mockito.*;

import javax.jms.JMSException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for {@link Session}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Jul 2016]
 */
@RunWith(MockitoJUnitRunner.class)
public class SessionTest {
	@Mock
	private Connection conn;
	@Mock
	private javax.jms.Session delegate;

	private Session session;

	@Before
	public void before() {
		this.session = new Session(this.conn, this.delegate);
	}

	@Test
	public void getProducer() throws JMSException {
		this.session.getProducer();

		verify(this.delegate).createProducer(null);

		this.session.getProducer();
	}

	@Test
	public void sent_frame() {
//		this.session.send(frame);
	}

	@Test
	public void send_message() {
//		this.session.send(message, subscription);
	}

	@After
	public void after() {
		verifyNoMoreInteractions(this.conn, this.delegate);
	}
}
