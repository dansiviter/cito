package cito;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [2 Feb 2017]
 */
public enum Strings { ;
	/**
	 * @param s the string to check.
	 * @return {@code true} if the given string is {@code null} or is the empty string.
	 */
	public static boolean isNullOrEmpty(String s) {
		return s == null || s.isEmpty();
	}
}
