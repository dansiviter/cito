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
package cito;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;

/**
 * General utility methods. Yea, I know someone will think this is an anti pattern, but meh!
 * 
 * @author Daniel Siviter
 * @since v1.0 [9 Aug 2016]
 */
public enum Util { ;
	/**
	 * 
	 * @param t
	 * @return
	 */
	@SafeVarargs
	public static <T> T getFirst(T... t) {
		return t != null && t.length > 0 ? t[0] : null;
	}

	/**
	 * 
	 * @param t
	 * @return
	 */
	public static <T> T getFirst(Collection<T> t) {
		return t == null || t.isEmpty() ? null : t.iterator().next();
	}

	/**
	 * 
	 * @param s character sequence to check.
	 * @return {@code true} if the given sequence is NOT {@code null} and is the empty string.
	 */
	public static boolean isEmpty(CharSequence s) {
		return s != null && s.length() == 0;
	}

	/**
	 * @param s character sequence to check.
	 * @return {@code true} if the given sequence is {@code null} or is the empty string.
	 */
	public static boolean isNullOrEmpty(CharSequence s) {
		return s == null || s.length() == 0;
	}

	/**
	 * 
	 * @param collection
	 * @return
	 */
	public static <C extends Collection<?>> C requireNonEmpty(C collection) {
		if (requireNonNull(collection).isEmpty()) {
			throw new IllegalArgumentException("Collection is empty!");
		}
		return collection;
	}

	/**
	 * 
	 * @param annotation
	 * @param annocations
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <A extends Annotation> A[] getAnnotations(Class<A> annotation, Collection<? extends Annotation> annotations) {
		final Collection<A> found = new ArrayList<>();
		for (Annotation a : annotations) {
			if (annotation.isAssignableFrom(annotation)) {
				found.add(annotation.cast(a));
			}
		}
		return found.toArray((A[]) Array.newInstance(annotation, 0));
	}
}
