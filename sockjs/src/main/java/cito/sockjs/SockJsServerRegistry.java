package cito.sockjs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;

import cito.stomp.server.AbstractServer;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [3 Jan 2017]
 */
@ApplicationScoped
public class SockJsServerRegistry {
	private final Map<String, AbstractServer> servers = new ConcurrentHashMap<>();

	/**
	 * 
	 * @param sessionId
	 * @param server
	 */
	public void register(String sessionId, AbstractServer server) {
		this.servers.computeIfAbsent(sessionId, k -> server);
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public AbstractServer get(String id) {
		return this.servers.get(id);
	}

	/**
	 * 
	 * @param sessionId
	 * @param server
	 */
	public void unregister(String sessionId, AbstractServer server) {
		this.servers.computeIfPresent(sessionId, (k, v) -> null);
	}
}
