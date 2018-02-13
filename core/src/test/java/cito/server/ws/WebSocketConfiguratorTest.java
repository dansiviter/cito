package cito.server.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link WebSocketConfigurator}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [11 Feb 2018]
 */
public class WebSocketConfiguratorTest {
	private WebSocketConfigurator configurator;

	@Before
	public void before() {
		this.configurator = new WebSocketConfigurator();
	}

	@Test
	public void modifyHandshake() {
		final ServerEndpointConfig sec = mock(ServerEndpointConfig.class);
		final HandshakeRequest request = mock(HandshakeRequest.class);
		final HandshakeResponse response = mock(HandshakeResponse.class);
		final Principal principal = mock(Principal.class);
		when(request.getUserPrincipal()).thenReturn(principal);
		final HttpSession session = mock(HttpSession.class);
		when(request.getHttpSession()).thenReturn(session);
		final Map<String, Object> props = new HashMap<>();
		when(sec.getUserProperties()).thenReturn(props);
		when(session.getId()).thenReturn("sessionId");

		this.configurator.modifyHandshake(sec, request, response);

		assertEquals(1, props.size());
		assertNotNull(props.get("SecurityContextsessionId"));

		verify(request).getHttpSession();
		verify(request).getUserPrincipal();
		verify(sec).getUserProperties();
		verify(session).getId();
		verifyNoMoreInteractions(sec, request, response, session, principal);
	}

	@Test
	public void modifyHandshake_noPrincipal() {
		final ServerEndpointConfig sec = mock(ServerEndpointConfig.class);
		final HandshakeRequest request = mock(HandshakeRequest.class);
		final HandshakeResponse response = mock(HandshakeResponse.class);
		final HttpSession session = mock(HttpSession.class);
		when(request.getHttpSession()).thenReturn(session);
		when(request.getUserPrincipal()).thenReturn(null);

		this.configurator.modifyHandshake(sec, request, response);

		verify(request).getUserPrincipal();
		verifyNoMoreInteractions(sec, request, response, session);
	}
}
