package cito.sockjs;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [3 Jan 2017]
 */
@RunWith(MockitoJUnitRunner.class)
public class UtilTest {
	@Mock
	private HttpServletRequest request;
	@Mock
	private Config config;

	@Before
	public void before() {
		when(this.config.path()).thenReturn("acme");
	}

	@Test
	public void session() {
		when(this.request.getRequestURI()).thenReturn("/acme/000/111/xhr");
		assertEquals("111", Util.session(config, request));

		when(this.request.getRequestURI()).thenReturn("/acme/000/Aa1Bb2Cc3/eventsource");
		assertEquals("Aa1Bb2Cc3", Util.session(this.config, this.request));

		verify(this.request, times(2)).getRequestURI();
		verify(this.config, times(2)).path();
	}

	@After
	public void after() {
		verifyNoMoreInteractions(this.request, this.config);
	}
}
