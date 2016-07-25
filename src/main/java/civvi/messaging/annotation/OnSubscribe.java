package civvi.messaging.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [12 Jul 2016]
 */
@Qualifier
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
public @interface OnSubscribe {
	/**
	 * Represents a topic pattern.
	 * 
	 * @return
	 */
	@Nonbinding
	String value() default "";
}
