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
package cito;

import static cito.ReflectionUtil.getAnnotation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

import org.junit.Test;
import org.slf4j.Logger;

/**
 * Unit tests for {@link LogProducer}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [14 Apr 2017]
 */
public class LogProducerTest {
	@Test
	public void scope() {
		assertNotNull(getAnnotation(LogProducer.class, ApplicationScoped.class));
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void logger_injectionPoint() {
		final InjectionPoint ip = mock(InjectionPoint.class);
		final Bean bean = mock(Bean.class);
		when(ip.getBean()).thenReturn(bean);
		when(bean.getBeanClass()).thenReturn(LogProducerTest.class);

		final Logger log = LogProducer.logger(ip);
		assertEquals("NOP", log.getName());

		verify(ip).getBean();
		verify(bean).getBeanClass();
		verifyNoMoreInteractions(ip, bean);
	}

	@Test
	public void logger_class() {
		final Logger log = LogProducer.logger(LogProducerTest.class);
		assertEquals("NOP", log.getName()); // only NOPLogger on the classpath
	}
}
