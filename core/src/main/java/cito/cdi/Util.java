package cito.cdi;

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
	public static void injectFields(BeanManager beanManager, Object instance) {
		if (instance == null) {
			throw new IllegalArgumentException("'instance' cannot be null!");
		}
		CreationalContext<?> creationalContext = beanManager.createCreationalContext(null);

		final AnnotatedType<?> annotatedType = beanManager.createAnnotatedType(instance.getClass());
		final InjectionTarget injectionTarget = beanManager.createInjectionTarget(annotatedType);
		injectionTarget.inject(instance, creationalContext);
	}
}
