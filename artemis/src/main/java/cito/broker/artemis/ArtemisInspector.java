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
package cito.broker.artemis;

import static org.apache.activemq.artemis.api.core.management.ResourceNames.BROKER;
import static org.apache.activemq.artemis.api.jms.management.JMSManagementHelper.putAttribute;
import static org.apache.activemq.artemis.api.jms.management.JMSManagementHelper.putOperationInvocation;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;

import org.apache.activemq.artemis.api.core.JsonUtil;
import org.apache.activemq.artemis.api.core.management.ResourceNames;
import org.apache.activemq.artemis.core.config.Configuration;

import cito.broker.Inspector;
import cito.jms.JmsContextHelper;
import cito.jms.Requestor;

/**
 * @author Daniel Siviter
 * @since v1.0 [28 Apr 2017]
 */
@ApplicationScoped
public class ArtemisInspector extends JmsContextHelper implements Inspector {

	@Inject
	private Configuration artemisConfig;

	/**
	 * 
	 * @return
	 * @throws JMSException
	 */
	public Set<Connection> getConnections() throws JMSException {
		return withRequestor(r -> {
			final Message req = r.context().createMessage();
			putAttribute(req, BROKER, "listConnectionsAsJSON");
			final Message res = r.request(req, 1, TimeUnit.MINUTES);
			final Set<Connection> results = new HashSet<>();
			getResults(res).forEach(e -> {
				final JsonObject obj = (JsonObject) e;
				results.add(new Connection(
						obj.getJsonNumber("creationTime").longValue(),
						obj.getJsonNumber("sessionCount").intValue(),
						obj.getString("implementation"),
						obj.getString("connectionID"),
						obj.getString("clientAddress")));
			});
			return results;
		});
	}

	/**
	 * 
	 * @param connectionId
	 * @return
	 * @throws JMSException
	 */
	public Set<Consumer> getConsumers(String connectionId) throws JMSException {
		return withRequestor(r -> {
			final Message req = r.context().createMessage();
			if (connectionId != null) {
				putOperationInvocation(req, BROKER, "listConsumerAsJSON", connectionId);
			} else {
				putOperationInvocation(req, BROKER, "listAllConsumersAsJSON");
			}
			final Message res = r.request(req, 1, TimeUnit.MINUTES);
			final Set<Consumer> results = new HashSet<>();
			getResults(res).forEach(e -> {
				final JsonObject obj = (JsonObject) e;
				results.add(new Consumer(
						obj.getString("filter"),
						obj.getString("queueName"),
						obj.getJsonNumber("creationTime").longValue(),
						obj.getJsonNumber("deliveringCount").intValue(),
						obj.getJsonNumber("consumerID").intValue(),
						obj.getBoolean("browseOnly"),
						obj.getString("connectionID"),
						obj.getString("sessionID")));
			});
			return results;
		});
	}

	/**
	 * 
	 * @param connectionId
	 * @return
	 * @throws JMSException
	 */
	public Set<Session> getSessions(String connectionId) throws JMSException {
		return withRequestor(r -> {
			final Message req = r.context().createMessage();
			if (connectionId != null) {
				putOperationInvocation(req, BROKER, "listSessionsAsJSON", connectionId);
			} else {
				putOperationInvocation(req, BROKER, "listAllConsumersAsJSON");
			}
			final Message res = r.request(req, 1, TimeUnit.MINUTES);
			final Set<Session> results = new HashSet<>();
			getResults(res).forEach(e -> {
				final JsonObject obj = (JsonObject) e;
				results.add(new Session(
						obj.getString("sessionID"),
						obj.getJsonNumber("creationTime").longValue(),
						obj.getJsonNumber("consumerCount").intValue(),
						obj.getString("principal")));
			});
			return results;
		});
	}

	/**
	 * 
	 * @param queue
	 * @return
	 * @throws JMSException
	 */
	public String getAddress(String queue) throws JMSException {
		return withRequestor(r -> {
			final Message req = r.context().createMessage();
			putOperationInvocation(req, ResourceNames.QUEUE + queue, "listConsumerAsJSON");
			final Message res = r.request(req, 1, TimeUnit.MINUTES);
			return ((JsonString) getResults(res).get(0)).getString();
		});
	}

	/**
	 * 
	 * @param f
	 * @return
	 * @throws JMSException
	 */
	private <R> R withRequestor(RequestionFunction<R> f) throws JMSException {
		final JMSContext ctx = getContext();
		final Topic topic = ctx.createTopic(this.artemisConfig.getManagementNotificationAddress().toString());

		try (Requestor requestor = new Requestor(ctx, topic)) {
			return f.apply(requestor);
		}
	}


	// --- Static Methods ---

	/**
	 * 
	 * @param message
	 * @return
	 * @throws JMSException
	 */
	public static JsonArray getResults(final Message message) throws JMSException {
		if (!(message instanceof TextMessage)) {
			throw new JMSException("Text message expected! [" + message + "]");
		}
		return JsonUtil.readJsonArray(((TextMessage) message).getText());
	}


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [28 Apr 2017]
	 * @param <R>
	 */
	@FunctionalInterface
	private interface RequestionFunction<R> {
		R apply(Requestor r) throws JMSException;
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [28 Apr 2017]
	 */
	public static class Connection {
		public final long creationTime;
		public final int sessionCount;
		public final String implementation;
		public final String connectionID;
		public final String clientAddress;

		public Connection(long creationTime, int sessionCount, String implementation, String connectionID, String clientAddress) {
			this.creationTime = creationTime;
			this.sessionCount = sessionCount;
			this.implementation = implementation;
			this.connectionID = connectionID;
			this.clientAddress = clientAddress;
		}
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [28 Apr 2017]
	 */
	public static class Consumer {
		public final String filter;
		public final String queueName;
		public final long creationTime;
		public final int deliveringCount;
		public final int consumerID;
		public final boolean browseOnly;
		public final String connectionID;
		public final String sessionID;

		public Consumer(
				String filter,
				String queueName,
				long creationTime,
				int deliveringCount,
				int consumerID,
				boolean browseOnly,
				String connectionID,
				String sessionID)
		{
			this.filter = filter;
			this.queueName = queueName;
			this.creationTime = creationTime;
			this.deliveringCount = deliveringCount;
			this.consumerID = consumerID;
			this.browseOnly = browseOnly;
			this.connectionID = connectionID;
			this.sessionID = sessionID;
		}
	}


	public static class Session {
		public final String sessionId;
		public final long creationTime;
		public final int consumerCount;
		public final String principal;

		public Session(String sessionId, long creationTime, int consumerCount, String principal) {
			this.sessionId = sessionId;
			this.creationTime = creationTime;
			this.consumerCount = consumerCount;
			this.principal = principal;
		}
	}
}

