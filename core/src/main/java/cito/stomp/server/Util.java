package cito.stomp.server;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;

/**
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
		return t != null && t.size() > 0 ? t.iterator().next() : null;
	}

	/**
	 * 
	 * @param t
	 * @return
	 */
	public static boolean isEmpty(CharSequence s) {
		return s != null && s.length() == 0;
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
