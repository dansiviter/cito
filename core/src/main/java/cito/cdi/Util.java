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
package cito.cdi;

import static java.util.Objects.*;

import javax.annotation.Nonnull;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [28 Nov 2016]
 */
public enum Util { ;
	/**
	 * Performs dependency injection on an instance. Useful for instances which aren't managed by CDI.
	 * <p/>
	 * <b>Attention:</b><br/>
	 * The resulting instance isn't managed by CDI; only fields annotated with @Inject get initialized.
	 *
	 * @param instance current instance
	 * @param <T>      current type
	 *
	 * @return instance with injected fields (if possible - or null if the given instance is null)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void injectFields(@Nonnull BeanManager beanManager, @Nonnull Object instance) {
		requireNonNull(beanManager);
		requireNonNull(instance);

		final CreationalContext<?> creationalContext = beanManager.createCreationalContext(null);
		final AnnotatedType<?> annotatedType = beanManager.createAnnotatedType(instance.getClass());
		final InjectionTarget injectionTarget = beanManager.createInjectionTarget(annotatedType);
		injectionTarget.inject(instance, creationalContext);
	}
}
