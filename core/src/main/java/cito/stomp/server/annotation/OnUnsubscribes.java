package cito.stomp.server.annotation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [2 Nov 2016]
 */
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface OnUnsubscribes {
	OnUnsubscribe[] value();
}
