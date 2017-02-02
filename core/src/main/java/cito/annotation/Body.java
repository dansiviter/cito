package cito.annotation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

import cito.event.MessageEvent;
import cito.stomp.Frame;

/**
 * Injects the {@link Frame#getBody()} from the {@link MessageEvent#frame()}, performing deserialisation if required.
 * <pre>
 * 	public void onSend(&#064;Observes &#064;OnSend MessageEvent, &#064;Body MyBean myBean) { ... }
 * </pre>
 * 
 * @author Daniel Siviter
 * @since v1.0 [25 Jan 2017]
 */
@Qualifier
@Target({ PARAMETER, METHOD })
@Retention(RUNTIME)
public @interface Body { }
