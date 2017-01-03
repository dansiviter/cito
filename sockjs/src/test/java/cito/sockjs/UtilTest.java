package cito.sockjs;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [3 Jan 2017]
 */
@RunWith(MockitoJUnitRunner.class)
public class UtilTest {
	@Test
	public void session() {
		final HttpServletRequest r = mock(HttpServletRequest.class);

		when(r.getRequestURI()).thenReturn("/blagh/000/111/xhr");
		assertEquals("111", Util.session(r));

		when(r.getRequestURI()).thenReturn("/blagh/000/Aa1Bb2Cc3/eventsource");
		assertEquals("Aa1Bb2Cc3", Util.session(r));

		verify(r, times(2)).getRequestURI();
		verifyNoMoreInteractions(r);
	}
}
