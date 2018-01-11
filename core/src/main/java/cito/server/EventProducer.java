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
package cito.server;

import static cito.Util.getAnnotations;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.inject.Inject;

import cito.DestinationType;
import cito.Glob;
import cito.PathParamProducer;
import cito.QuietClosable;
import cito.ReflectionUtil;
import cito.annotation.OnConnected;
import cito.annotation.OnDisconnect;
import cito.annotation.OnSend;
import cito.annotation.OnSubscribe;
import cito.annotation.OnUnsubscribe;
import cito.event.Message;

/**
 * Fires off events related to destinations.
 * 
 * @author Daniel Siviter
 * @since v1.0 [27 Jul 2016]
 */
@ApplicationScoped
public class EventProducer {
	private final Map<String, String> idDestinationMap = new WeakHashMap<>();

	@Inject
	private BeanManager manager;

	/**
	 * 
	 * @param msg
	 */
	public void message(@Observes Message msg) {
		if (msg.frame().isHeartBeat()) return;

		final Extension extension = this.manager.getExtension(Extension.class);

		switch (msg.frame().command()) {
		case CONNECTED: { // on client thread as it's response to CONNECT
			extension.getMessageObservers(OnConnected.class).forEach(om -> om.notify(msg));
			break;
		}
		case SEND: {
			final String destination = msg.frame().destination().get();
			final boolean consumed = notify(OnSend.class, extension.getMessageObservers(OnSend.class), destination, msg);
			if (!consumed && DestinationType.from(destination) == DestinationType.DIRECT) {
				throw new IllegalStateException("Non-JMS destination not consumed! [destination=" + destination + ",sessionId=" + msg.sessionId() + "]");
			}
			break;
		}
		case SUBSCRIBE: {
			final String id = msg.frame().subscription().get();
			final String destination = msg.frame().destination().get();
			idDestinationMap.put(id, destination);
			notify(OnSubscribe.class, extension.getMessageObservers(OnSubscribe.class), destination, msg);
			break;
		}
		case UNSUBSCRIBE: {
			final String id = msg.frame().subscription().get();
			final String destination = this.idDestinationMap.remove(id);
			notify(OnUnsubscribe.class, extension.getMessageObservers(OnUnsubscribe.class), destination, msg);
			break;
		}
		case DISCONNECT: {
			extension.getMessageObservers(OnDisconnect.class).forEach(om -> om.notify(msg));
			break;
		}
		default:
			break;
		}
	}


	// --- Static Methods ---

	/**
	 * 
	 * @param annotation
	 * @param observerMethods
	 * @param destination
	 * @param evt
	 * @return {@code true} if one or more notifiers consumed the event.
	 */
	private static <A extends Annotation> boolean notify(Class<A> annotation, Set<ObserverMethod<Message>> observerMethods, String destination, Message evt) {
		boolean consumed = false;
		for (ObserverMethod<Message> om : observerMethods) {
			for (A a : getAnnotations(annotation, om.getObservedQualifiers())) {
				final String value = ReflectionUtil.invoke(a, "value");
				if (!Glob.from(value).matches(destination)) {
					continue;
				}
				try (QuietClosable closable = PathParamProducer.set(value)) {
					om.notify(evt);
					consumed = true;
				}
			}
		}
		return consumed;
	}
}
