package cito.stomp.server.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

import cito.stomp.Command;
import cito.stomp.Glob;
import cito.stomp.server.event.MessageEvent;

/**
 * Observable {@link MessageEvent} for when a user performs a {@link Command#SUBSCRIBE} to a destination.
 * 
 * <pre>
 * 	public void on(&#064;Observes &#064;OnSubscribe("/topic/{param}.world}") MessageEvent e) {
 * 		// do something
 * 	}
 * </pre>
 *
 * @author Daniel Siviter
 * @since v1.0 [12 Jul 2016]
 */
@Qualifier
@Target({ PARAMETER, METHOD })
@Retention(RUNTIME)
@Repeatable(OnSubscribes.class)
public @interface OnSubscribe {
	/**
	 * A GLOB expression of the topic pattern required.
	 * 
	 * @return
	 * @see Glob
	 */
	@Nonbinding
	String value() default "**";
}
