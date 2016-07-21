package civvi.messaging.annotation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [19 Jul 2016]
 */
@Qualifier
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface OnOpen { }
