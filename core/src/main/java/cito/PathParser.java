package cito;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for parsing path parameters.
 * 
 * @author Daniel Siviter
 * @since v1.0 [17 Jan 2017]
 */
public class PathParser {
	private final String separator;
	private final String pattern;
	private final String[] segments;

	private final Map<Integer, String> segmentMap;

	/**
	 * Create a parser that uses forward slashes ('/') and period/full-stop ('.') as separators.
	 * 
	 * @param pattern the pattern to parse.
	 */
	public PathParser(String pattern) {
		this("/|\\.", pattern);
	}

	/**
	 * 
	 * @param separator
	 * @param pattern
	 */
	public PathParser(String separator, String pattern) {
		if (separator.contains("{") || separator.contains("}")) {
			throw new IllegalArgumentException("Separators cannot include '{' or '}'!");
		}
		this.separator = separator;
		this.pattern = pattern;
		this.segments = pattern.split(separator);
		final Map<Integer, String> segmentMap = new HashMap<>();
		for (int i = 0; i < this.segments.length; i++) {
			final String segment = this.segments[i];
			if (isParam(segment)) {
				segmentMap.put(i, segment.substring(1, segment.length() - 1));
			}
		}
		this.segmentMap = Collections.unmodifiableMap(segmentMap);
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	public Map<String, String> parse(String path) {
		final String[] segments = path.split(this.separator);
		if (segments.length != this.segments.length) {
			throw new IllegalArgumentException("Different number of segments! [pattern=" + this.pattern + ",path=" + path + "]");
		}
		final Map<String, String> params = new HashMap<>();
		for (int i = 0; i < this.segments.length; i++) {
			final String paramName = this.segmentMap.get(i);
			if (paramName != null) {
				params.put(paramName, segments[i]);
				continue;
			}
			if (!this.segments[i].equals(segments[i])) {
				throw new IllegalArgumentException(
						"Unnamed segment does not match pattern! [pattern=" + this.pattern + ",path=" + path + ",segment=" + i + "]");
			}
		}
		return params;
	}


	// --- Static Methods ---

	/**
	 * 
	 * @param segment
	 * @return
	 */
	private static boolean isParam(String segment) {
		return segment.startsWith("{") && segment.endsWith("}");
	}

	/**
	 * 
	 * @param pattern
	 * @return
	 */
	public static PathParser create(String pattern) {
		return new PathParser(pattern);
	}

	/**
	 * 
	 * @param pattern
	 * @param path
	 * @return
	 */
	public static Map<String, String> parse(String pattern, String path) {
		return create(pattern).parse(path);
	}
}
