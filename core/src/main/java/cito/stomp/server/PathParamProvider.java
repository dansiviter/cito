package cito.stomp.server;

import java.util.Map;
import java.util.WeakHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import cito.DestinationEvent;
import cito.PathParser;
import cito.PathParser.Result;
import cito.QuietClosable;
import cito.stomp.server.annotation.PathParam;
import cito.stomp.server.event.MessageEvent;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [23 Nov 2016]
 */
@ApplicationScoped
public class PathParamProvider {
	private static final Map<String, PathParser> PARSERS = new WeakHashMap<>();
	private static final ThreadLocal<PathParser> HOLDER = new ThreadLocal<>();

	@Produces @Dependent
	public static PathParser get() {
		return HOLDER.get();
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	public static QuietClosable set(String path) {
		return set(pathParser(path));
	}

	/**
	 * 
	 * @param e
	 */
	public static QuietClosable set(PathParser e) {
		final PathParser old = get();
		if (old != null) {
			throw new IllegalStateException("Already set!");
		}
		HOLDER.set(e);
		return new QuietClosable() {
			@Override
			public void close() {
				HOLDER.remove();
			}
		};
	}

	/**
	 * 
	 * @param ip
	 * @param e
	 * @return
	 */
	public static PathParser pathParser(String path) {
		return PARSERS.computeIfAbsent(path, (k) -> PathParser.create(path));
	}

	/**
	 * 
	 * @param ip
	 * @param parser
	 * @param e
	 * @return
	 */
	@Produces @Dependent @PathParam("nonbinding")
	public static String pathParam(InjectionPoint ip, PathParser parser, MessageEvent me, DestinationEvent de) {
		final String destination;
		if (me != null) {
			destination = me.frame().destination();
		} else if (de != null) {
			destination = de.getDestination();
		} else {
			throw new IllegalStateException("Neither MessageEvent or DestinationEvent is resolvable!");
		}

		final PathParam param = ip.getAnnotated().getAnnotation(PathParam.class);
		final Result r = parser.parse(destination);
		if (r.isSuccess()) {
			return r.get(param.value());
		}
		return null;
	}
}
