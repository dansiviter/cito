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

import static org.apache.deltaspike.core.api.provider.BeanProvider.getContextualReference;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessInjectionPoint;

import org.apache.deltaspike.core.util.ReflectionUtils;
import org.apache.deltaspike.core.util.bean.BeanBuilder;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;
import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;

import cito.annotation.Body;
import cito.event.Message;
import cito.ext.Serialiser;
import cito.io.ByteBufferInputStream;
import cito.stomp.Frame;

/**
 * An extension to process {@link Body} annotations for injection of deserialised values.
 * 
 * @author Daniel Siviter
 * @since v1.0 [3 May 2017]
 */
public class BodyProducerExtension implements Extension {
	private final List<Bean<?>> found = new ArrayList<>();

	/**
	 * 
	 * @param pip
	 * @param beanManager
	 */
	public void captureProducerTypes(@Observes final ProcessInjectionPoint<?, ?> pip, BeanManager beanManager) {
		if (pip.getInjectionPoint().getAnnotated().isAnnotationPresent(Body.class)) {
			found.add(createBeanAdapter(pip.getInjectionPoint(), beanManager));
		}
	}

	/**
	 * 
	 * @param abd
	 * @param beanManager
	 */
	public void addBeans(@Observes final AfterBeanDiscovery abd, BeanManager beanManager) {
		this.found.forEach(abd::addBean);
		this.found.clear();
	}

	/**
	 * 
	 * @param ip
	 * @param beanManager
	 * @return
	 */
	private <T> Bean<T> createBeanAdapter(InjectionPoint ip, BeanManager beanManager) {
		final Type type = ip.getType();
		final Class<T> rawType = ReflectionUtils.getRawType(type);
		final ContextualLifecycle<T> lifecycleAdapter = new BodyLifecycle<T>(type, beanManager);
		final BeanBuilder<T> beanBuilder = new BeanBuilder<T>(beanManager)
				.readFromType(new AnnotatedTypeBuilder<T>().readFromType(rawType).create())
				.beanClass(Body.class) // see https://issues.jboss.org/browse/WELD-2165
				.name(ip.getMember().getName())
				.qualifiers(ip.getQualifiers())
				.beanLifecycle(lifecycleAdapter)
				.scope(Dependent.class)
				.passivationCapable(false)
				.alternative(false)
				.nullable(true)
				.id("BodyBean#" + type.toString())
				.addType(type); //java.lang.Object needs to be present (as type) in any case
		return beanBuilder.create();
	}


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [3 May 2017]
	 * @param <T>
	 */
	private static class BodyLifecycle<T> implements ContextualLifecycle<T> {
		private final Type type;
		private final BeanManager beanManager;

		/**
		 * 
		 * @param type
		 * @param beanManager
		 */
		public BodyLifecycle(Type type, BeanManager beanManager) {
			this.type = type;
			this.beanManager = beanManager;
		}

		@Override
		public T create(Bean<T> bean, CreationalContext<T> creationalContext) {
			final Frame frame = getContextualReference(this.beanManager, Message.class, false).frame();
			final Serialiser serialiser = getContextualReference(this.beanManager, Serialiser.class, false);
			try (InputStream is = new ByteBufferInputStream(frame.getBody())) {
				return serialiser.readFrom(this.type, frame.contentType(), is);
			} catch (IOException e) {
				throw new IllegalStateException("Unable to serialise!", e);
			}
		}

		@Override
		public void destroy(Bean<T> bean, T instance, CreationalContext<T> creationalContext) { }
	}
}
