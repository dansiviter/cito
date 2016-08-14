package cito.stomp.server.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

import cito.DestinationEvent.Type;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [18 Jul 2016]
 */
@Qualifier
@Target({ PARAMETER, FIELD })
@Retention(RUNTIME)
public @interface OnDestination {
	/**
	 * 
	 * @return
	 */
	Type type();
}
