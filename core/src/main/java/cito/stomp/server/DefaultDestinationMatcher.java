package cito.stomp.server;

import java.util.Map;
import java.util.WeakHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Specializes;

import cito.stomp.Glob;

/**
 * Defines the default matcher which performs GLOB matching and caching of patterns. To override, use
 * {@link Alternative} or {@link Specializes} mechanisms (see
 * <a href="http://docs.oracle.com/javaee/6/tutorial/doc/gjsdf.html">here</a>.
 * 
 * @author Daniel Siviter
 * @since v1.0 [7 Sep 2016]
 */
@ApplicationScoped
public class DefaultDestinationMatcher implements DestinationMatcher {
	private final Map<String, Glob> globs = new WeakHashMap<>();

	@Override
	public boolean matches(String test, String destination) {
		final Glob glob = this.globs.computeIfAbsent(test, k -> new Glob(test));
		return glob.matches(destination);
	}
}
