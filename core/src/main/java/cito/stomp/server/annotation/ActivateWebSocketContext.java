package cito.stomp.server.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.interceptor.InterceptorBinding;


@InterceptorBinding
@Retention(RUNTIME)
@Target({ METHOD, TYPE })
public @interface ActivateWebSocketContext {

    public static class Literal extends AnnotationLiteral<ActivateWebSocketContext> implements ActivateWebSocketContext {

        public static final Literal INSTANCE = new Literal();

        private static final long serialVersionUID = 1L;
    }
}
