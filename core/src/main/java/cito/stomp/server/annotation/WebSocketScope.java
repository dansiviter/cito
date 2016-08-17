package cito.stomp.server.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Scope;

/**
 * TODO document when this scope will be active. This will behave slightly differently for SockJS.
 * 
 * 
 * @author Daniel Siviter
 * @since v1.0 [17 Aug 2016]
 */
@Target({ TYPE, METHOD, FIELD })
@Retention(RUNTIME)
@Documented
@Scope
@Inherited
public @interface WebSocketScope { }
