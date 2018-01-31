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
package cito.jms;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.Topic;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Unit test for {@link Requestor}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [20 Apr 2017]
 */
public class RequestorTest {
	@Rule
	public MockitoRule mockito = MockitoJUnit.rule();

	@Mock
	private JMSContext context;
	@Mock
	private JMSConsumer consumer;
	@Mock
	private JMSProducer producer;

	@Before
	public void before() {
		when(this.context.createConsumer(Mockito.any())).thenReturn(this.consumer);
		when(this.context.createProducer()).thenReturn(this.producer);
	}

	@Test
	public void topic() throws JMSException {
		final Destination dest = Mockito.mock(Topic.class);
		final TemporaryTopic tempDest = mock(TemporaryTopic.class);
		when(this.context.createTemporaryTopic()).thenReturn(tempDest);

		test(dest, tempDest);

		verify(this.context).createTemporaryTopic();
		verify(tempDest).delete();
		verifyNoMoreInteractions(dest);
	}

	@Test
	public void queue() throws JMSException {
		final Destination dest = mock(Queue.class);
		final TemporaryQueue tempDest = mock(TemporaryQueue.class);
		when(this.context.createTemporaryQueue()).thenReturn(tempDest);

		test(dest, tempDest);

		verify(this.context).createTemporaryQueue();
		verify(tempDest).delete();
		verifyNoMoreInteractions(dest, tempDest);
	}

	private void test(Destination dest, Destination tempDest) throws JMSException {
		final Message message = mock(Message.class);
		try (Requestor requestor = new Requestor(context, dest)) {
			requestor.request(message);
			requestor.request(message, 1, TimeUnit.SECONDS);
		}

		verify(this.context).createConsumer(dest);
		verify(message, times(2)).setJMSReplyTo(tempDest);
		verify(this.producer, times(2)).send(dest, message);
		verify(this.consumer).receive();
		verify(this.consumer).receive(1_000);
		verifyNoMoreInteractions(message);
	}

	@After
	public void after() {
		verify(this.context).createProducer();
		verify(this.consumer).close();
		verifyNoMoreInteractions(this.context, this.consumer, this.producer);
	}
}
