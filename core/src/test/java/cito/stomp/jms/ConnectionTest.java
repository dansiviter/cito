package cito.stomp.jms;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.websocket.CloseReason;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import cito.stomp.Frame;
import cito.stomp.server.event.MessageEvent;

/**
 * Unit tests for {@link Connection}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Jul 2016]
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectionTest {
	@Mock
	private Factory factory;

	@InjectMocks
	private Connection connection;

	@Test
	public void init() {
		this.connection.init();
	}

	@Test
	public void send_frame() {
		final Frame frame = mock(Frame.class);

		this.connection.send(frame);

		verifyNoMoreInteractions(frame);
	}

	//	@Test
	//	private Session getSession(Frame in) throws JMSException {

	@Test
	public void connect() throws JMSException {
		final MessageEvent messageEvent = mock(MessageEvent.class);

		this.connection.connect(messageEvent);

		verifyNoMoreInteractions(messageEvent);
	}

	@Test
	public void on_messageEvent() {
		final MessageEvent messageEvent = mock(MessageEvent.class);

		this.connection.on(messageEvent);

		verifyNoMoreInteractions(messageEvent);
	}

	@Test
	public void addAckMessage() throws JMSException {
		final Message msg = mock(Message.class);

		this.connection.addAckMessage(msg);

		verifyNoMoreInteractions(msg);
	}

	@Test
	public void close_closeReason() throws IOException {
		final CloseReason reason = mock(CloseReason.class);

		this.connection.close(reason);

		verifyNoMoreInteractions(reason);
	}
}
