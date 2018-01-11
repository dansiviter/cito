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

import java.util.Map;
import java.util.WeakHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import cito.PathParser.Result;
import cito.annotation.PathParam;
import cito.event.DestinationChanged;
import cito.event.Message;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [23 Nov 2016]
 */
@ApplicationScoped
public class PathParamProducer {
	private static final Map<String, PathParser> PARSERS = new WeakHashMap<>();
	private static final ThreadLocal<PathParser> HOLDER = new ThreadLocal<>();

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
	 * @param parser
	 * @return
	 */
	public static QuietClosable set(PathParser parser) {
		final PathParser old = pathParser();
		if (old != null) {
			throw new IllegalStateException("Already set!");
		}
		HOLDER.set(parser);
		return () -> HOLDER.remove();
	}

	/**
	 * 
	 * @return
	 */
	@Produces @Dependent
	public static PathParser pathParser() {
		return HOLDER.get();
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	public static PathParser pathParser(String path) {
		return PARSERS.computeIfAbsent(path, k -> PathParser.create(path));
	}

	/**
	 * 
	 * @param ip
	 * @param parser
	 * @param msg
	 * @param dc
	 * @return
	 */
	@Produces @Dependent @PathParam("nonbinding")
	public static String pathParam(InjectionPoint ip, PathParser parser, Message msg, DestinationChanged dc) {
		final String destination;
		if (msg != null) {
			destination = msg.frame().destination().get();
		} else if (dc != null) {
			destination = dc.getDestination();
		} else {
			throw new IllegalStateException("Neither MessageEvent or DestinationEvent is resolvable!");
		}

		final PathParam param = ip
				.getAnnotated()
				.getAnnotation(PathParam.class);
		final Result r = parser.parse(destination);
		if (r.isSuccess()) {
			return r.get(param.value());
		}
		return null;
	}
}
