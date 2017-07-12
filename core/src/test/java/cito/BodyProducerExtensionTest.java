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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionPoint;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import cito.annotation.Body;


/**
 * Unit test for {@link BodyProducerExtension}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [21 May 2017]
 */
@RunWith(MockitoJUnitRunner.class)
public class BodyProducerExtensionTest {
	@SuppressWarnings("serial")
	private static final Map<String, String> COLLECTION = new HashMap<String, String>() { };

	@Mock
	private BeanManager beanManager;

	private BodyProducerExtension extension;

	@Before
	public void before() {
		this.extension = new BodyProducerExtension();
	}

	@Test
	public void captureProducerTypes() {
		final ProcessInjectionPoint<?, ?> pip = mock(ProcessInjectionPoint.class);
		final InjectionPoint ip = mock(InjectionPoint.class);
		when(pip.getInjectionPoint()).thenReturn(ip);
		final Annotated annotated = mock(Annotated.class);
		when(ip.getAnnotated()).thenReturn(annotated);
		when(annotated.isAnnotationPresent(Body.class)).thenReturn(true);
		when(ip.getType()).thenReturn(COLLECTION.getClass());
		@SuppressWarnings("unchecked")
		final InjectionTarget<Object> injectionTarget = mock(InjectionTarget.class);
		when(this.beanManager.createInjectionTarget(Mockito.any())).thenReturn(injectionTarget);
		final Member member = mock(Member.class);
		when(ip.getMember()).thenReturn(member);

		final List<Bean<?>> found = ReflectionUtil.get(this.extension, "found");
		assertTrue(found.isEmpty());

		this.extension.captureProducerTypes(pip, this.beanManager);

		assertEquals(1, found.size());

		verify(pip, times(2)).getInjectionPoint();
		verify(ip).getAnnotated();
		verify(annotated).isAnnotationPresent(Body.class);
		verify(ip).getType();
		verify(ip).getMember();
		verify(member).getName();
		verify(ip).getQualifiers();
		verify(injectionTarget).getInjectionPoints();
		verify(this.beanManager).createInjectionTarget(Mockito.any());
		verifyNoMoreInteractions(pip, ip, annotated, injectionTarget, member);
	}

	@Test
	public void addBeans() {
		final AfterBeanDiscovery abd = mock(AfterBeanDiscovery.class);
		final Bean<?> bean = mock(Bean.class);

		final List<Bean<?>> found = ReflectionUtil.get(this.extension, "found");
		found.add(bean);

		this.extension.addBeans(abd);

		assertTrue(found.isEmpty());

		verify(abd).addBean(bean);
		verifyNoMoreInteractions(abd, bean);
	}

	@After
	public void after() {
		verifyNoMoreInteractions(this.beanManager);
	}
}
