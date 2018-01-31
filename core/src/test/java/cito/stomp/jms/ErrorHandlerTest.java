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
package cito.stomp.jms;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import javax.enterprise.event.Event;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;
import org.slf4j.Logger;

import cito.event.Message;
import cito.stomp.Command;
import cito.stomp.Frame;

/**
 * Unit test for {@link ErrorHandler}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [28 Mar 2017]
 */
public class ErrorHandlerTest {
	@Rule
	public MockitoRule mockito = MockitoJUnit.rule();

	@Mock
	private Logger log;
	@Mock
	private Event<Message> messageEvent;

	@InjectMocks
	private ErrorHandler errorHandler;

	@Test
	public void onError() {
		final Relay relay = mock(Relay.class);
		final Frame cause = Frame.HEART_BEAT;
		final Exception e = new Exception("Oh no!");

		this.errorHandler.onError(relay, "sessionId", cause, "oooh", e);

		verify(this.log).warn("Error while processing frame! [sessionId={},frame.command={}]", "sessionId", Command.HEARTBEAT, e);
		verify(this.messageEvent).fire(any(Message.class));
		verify(relay).close("sessionId");
		verifyNoMoreInteractions(relay);
	}

	@After
	public void after() {
		verifyNoMoreInteractions(this.log, this.messageEvent);
	}
}
