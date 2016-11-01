package cito.stomp.server;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.security.Principal;
import java.util.Collections;
import java.util.concurrent.PriorityBlockingQueue;

import javax.enterprise.event.Event;
import javax.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import cito.stomp.ext.Serialiser;
import cito.stomp.server.event.MessageEvent;

/**
 * Unit test for {@link Support}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Jul 2016]
 */
@RunWith(MockitoJUnitRunner.class)
public class SupportTest {
	@Mock
	private Event<MessageEvent> msgEvent;
	@Mock
	private SessionRegistry registry;
	@Mock
	private Serialiser serialiser;
	@Mock
	private Principal principal;

	@InjectMocks
	private Concrete support;

	@Test
	public void broadcast_destination_payload() {
		this.support.broadcast("destination", new Object(), Collections.emptyMap());
	}

	@Test
	public void broadcast_destination_mediaType_payload() {
		this.support.broadcast("destination", MediaType.TEXT_PLAIN_TYPE, new Object(), Collections.emptyMap());
	}

	@Test
	public void broadcastTo_destination_payload() {
		this.support.broadcastTo(principal, "destination", new Object(), Collections.emptyMap());
	}

	@Test
	public void broadcastTo_destination_mediaType_payload() {
		this.support.broadcastTo(principal, "destination", MediaType.TEXT_PLAIN_TYPE, new Object(), Collections.emptyMap());
	}

	@Test
	public void sendTo_destination_payload() {
		this.support.sendTo("sessionId", "destination", new Object(), Collections.emptyMap());
	}

	@Test
	public void sendTo_destination_mediaType_payload() {
		this.support.sendTo("sessionId", "destination", MediaType.TEXT_PLAIN_TYPE, new Object(), Collections.emptyMap());
	}

	@After
	public void after() {
		verifyNoMoreInteractions(this.msgEvent, this.registry, this.serialiser, this.principal);
	}


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [31 Oct 2016]
	 */
	private static class Concrete extends Support { }
}
