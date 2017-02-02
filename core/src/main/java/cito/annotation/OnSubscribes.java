package cito.annotation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * {@link Repeatable} for {@link OnSubscribe}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [2 Nov 2016]
 */
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface OnSubscribes {
	OnSubscribe[] value();
}
