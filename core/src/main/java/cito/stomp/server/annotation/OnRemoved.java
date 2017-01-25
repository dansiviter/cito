package cito.stomp.server.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

/**
 *
 * @author Daniel Siviter
 * @since v1.0 [25 Jan 2017]
 */
@Qualifier
@Target({ PARAMETER, METHOD })
@Retention(RUNTIME)
public @interface OnRemoved {
	/**
	 * A regular expression of the topic pattern required.
	 * 
	 * @return
	 */
	@Nonbinding
	String value() default ".*";
}