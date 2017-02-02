package cito.broker.artemis;

import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [2 Feb 2017]
 */
public interface BrokerConfig {

	String IN_VM_CONNECTOR = InVMConnectorFactory.class.getName();
	String REMOTE_CONNECTOR = NettyConnectorFactory.class.getName();

	/**
	 * @return if present, sends a username for the connection
	 */
	String getUsername();

	/**
	 * @return the password for the connection.  If username is set, password must be set
	 */
	String getPassword();

	/**
	 * Either url should be set, or host, port, connector factory should be set.
	 *
	 * @return if set, will be used in the server locator to look up the server instead of the hostname/port combination
	 */
	String getUrl();

	/**
	 * @return The hostname to connect to
	 */
	String getHost();

	/**
	 * @return the port number to connect to
	 */
	Integer getPort();

	/**
	 * @return the connector factory to use for connections.
	 */
	String getConnectorFactory();

	/**
	 * @return Whether or not to start the embedded broker
	 */
	boolean startEmbeddedBroker();

	/**
	 * @return whether or not this is an HA connection
	 */
	boolean isHa();

	/**
	 * @return whether or not the authentication parameters should be used
	 */
	boolean hasAuthentication();

	/**
	 * @return the configuration that will be used in the embedded broker.
	 */
	Configuration getEmbeddedConfiguration();
}
