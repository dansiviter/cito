package cito.stomp.server.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

import cito.DestinationEvent;
import cito.stomp.Command;
import cito.stomp.Glob;

/**
 * Observable {@link DestinationEvent} for when the first user performs a {@link Command#SUBSCRIBE} to a destination.
 * 
 * <pre>
 * 	public void on(&#064;Observes &#064;OnAdded("/topic/{param}.world}") DestinationEvent e) {
 * 		// do something
 * 	}
 * </pre>
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Jan 2017]
 */
@Qualifier
@Target({ PARAMETER, METHOD })
@Retention(RUNTIME)
public @interface OnAdded {
	/**
	 * A GLOB expression of the topic pattern required.
	 * 
	 * @return
	 * @see Glob
	 */
	@Nonbinding
	String value() default "**";
}