package cito.stomp.server.annotation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Qualifies as from the server.
 * 
 * @author Daniel Siviter
 * @since v1.0 [19 Jul 2016]
 */
@Qualifier
@Target({ PARAMETER, FIELD })
@Retention(RUNTIME)
public @interface FromServer { }
