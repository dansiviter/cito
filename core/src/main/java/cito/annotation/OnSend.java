package cito.annotation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

import cito.Glob;
import cito.event.MessageEvent;
import cito.stomp.Command;

/**
 * Observable qualifier {@link MessageEvent} for when a user performs a {@link Command#SEND}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [12 Jul 2016]
 */
@Qualifier
@Target(PARAMETER)
@Retention(RUNTIME)
@Repeatable(OnSends.class)
public @interface OnSend {
	/**
	 * A GLOB expression of the topic pattern required.
	 * 
	 * @return
	 * @see Glob
	 */
	@Nonbinding
	String value() default "**";
}
