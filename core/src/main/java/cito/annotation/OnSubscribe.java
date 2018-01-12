/*
 * Copyright 2016-2017 Daniel Siviter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cito.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

import cito.Glob;
import cito.event.Message;
import cito.stomp.Command;

/**
 * Observable {@link Message} for when a user performs a {@link Command#SUBSCRIBE} to a destination.
 * 
 * <pre>
 * 	public void on(&#064;Observes &#064;OnSubscribe("topic/{param}.world}") MessageEvent e) {
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


	// --- Inner Classes ---

	/**
	 * Literal for {@link OnSubscribe}.
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [13 Jan 2018]
	 */
	public static final class Literal extends AnnotationLiteral<OnSubscribe> implements OnSubscribe {
		private static final long serialVersionUID = 1L;

		private final String value;

		private Literal(String value) {
			this.value = value != null ? value : "**";
		}

		@Override
		public String value() {
			return this.value;
		}

		public static OnSubscribe onSubscribe(String value) {
			return new Literal(value);
		}
	}
}
