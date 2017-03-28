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
package cito.broker.artemis;

import static org.apache.activemq.artemis.api.core.management.ManagementHelper.HDR_NOTIFICATION_TYPE;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.enterprise.event.Event;
import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.activemq.artemis.api.core.management.CoreNotificationType;
import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;
import org.apache.activemq.artemis.jms.server.management.JMSNotificationType;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;


/**
 * Unit test for {@link EventProducer}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [17 Jan 2017]
 */
@RunWith(MockitoJUnitRunner.class)
public class EventProducerTest {
	@Mock
	private Logger log;
	@Mock
	private EmbeddedJMS broker;
	@Mock
	private Event<cito.event.DestinationChanged> destinationEvent;
	@Mock
	private Message message;

	@InjectMocks
	private EventProducer eventProducer;

	@Test
	public void unknownEvent() throws JMSException {
		for (CoreNotificationType type : CoreNotificationType.values()) {
			when(this.message.getStringProperty(HDR_NOTIFICATION_TYPE.toString())).thenReturn(type.name());
			this.eventProducer.onMessage(message);
		}
		when(this.message.getStringProperty(HDR_NOTIFICATION_TYPE.toString())).thenReturn(JMSNotificationType.CONNECTION_FACTORY_CREATED.name());
		this.eventProducer.onMessage(message);
		when(this.message.getStringProperty(HDR_NOTIFICATION_TYPE.toString())).thenReturn(JMSNotificationType.CONNECTION_FACTORY_DESTROYED.name());
		this.eventProducer.onMessage(message);

		verify(this.message, times(22)).getStringProperty(HDR_NOTIFICATION_TYPE.toString());
	}

	@After
	public void after() {
		verifyNoMoreInteractions(this.log, this.broker, this.destinationEvent, this.message);
	}
}
