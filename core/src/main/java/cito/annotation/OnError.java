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

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [29 Jan 2017]
 */
@Qualifier
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface OnError { 
	/**
	 * Literal for {@link OnError}.
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [13 Jan 2018]
	 */
	public static final class Literal extends AnnotationLiteral<OnError> implements OnError {
		private static final long serialVersionUID = 1L;
		private static final OnError INSTANCE = new Literal();

		public static OnError onError() {
			return INSTANCE;
		}
	}
}
