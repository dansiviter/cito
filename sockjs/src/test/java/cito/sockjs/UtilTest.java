/*
 * Copyright 2016-2017 Daniel Siviter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 * Unit test for {@link Util}.
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
