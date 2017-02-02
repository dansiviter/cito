package cito.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.context.NormalScope;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;

/**
 * Links a bean scope with that of a WebSocket Session. It will be active during the WebSocket lifecycle methods
 * {@link OnOpen}, {@link OnMessage}, {@link OnError}, and {@link OnClose}. The context is destroyed when
 * {@link OnClose} completes.
 * 
 * @author Daniel Siviter
 * @since v1.0 [17 Aug 2016]
 */
@Target({ TYPE, METHOD, FIELD })
@Retention(RUNTIME)
@Documented
@NormalScope
@Inherited
public @interface WebSocketScope { }
