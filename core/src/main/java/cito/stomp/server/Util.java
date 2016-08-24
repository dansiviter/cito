package cito.stomp.server;

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
}
