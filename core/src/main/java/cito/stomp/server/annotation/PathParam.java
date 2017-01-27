package cito.stomp.server.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

/**
 * Inject an expanded path parameter into the method:
 * <pre>
 * 	public void on(
 * 			&#064;Observes &#064;OnSend("{param}.world}") MessageEvent e,
 * 			&#064;PathParam("param") String param)
 * 	{
 * 		// do something
 * 	}
 * </pre>
 * 
 * @author Daniel Siviter
 * @since v1.0 [23 Nov 2016]
 */
@Qualifier
@Target({ PARAMETER, METHOD })
@Retention(RUNTIME)
public @interface PathParam {
	@Nonbinding
	String value();
}
