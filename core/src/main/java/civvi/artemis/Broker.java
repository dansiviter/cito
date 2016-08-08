package civvi.artemis;

import java.util.Collections;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory;
import org.apache.activemq.artemis.jms.server.config.ConnectionFactoryConfiguration;
import org.apache.activemq.artemis.jms.server.config.JMSConfiguration;
import org.apache.activemq.artemis.jms.server.config.impl.ConnectionFactoryConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.JMSConfigurationImpl;
import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [18 Jul 2016]
 */
@ApplicationScoped
public class Broker {
	@Inject
	private BeanManager beanManager;

	private EmbeddedJMS broker;

	public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
		try {
			final Configuration config = new ConfigurationImpl()
					.setPersistenceEnabled(false)
					.setSecurityEnabled(false);
			final JMSConfiguration jmsConfig = new JMSConfigurationImpl();
			final ConnectionFactoryConfiguration cfConfig = new ConnectionFactoryConfigurationImpl();

			for (Bean<?> bean : this.beanManager.getBeans(BrokerCustomiser.class)) {
				final CreationalContext<?> ctx = this.beanManager.createCreationalContext(bean);
				final BrokerCustomiser customiser = (BrokerCustomiser) this.beanManager.getReference(
						bean, BrokerCustomiser.class, ctx);
				customiser.customise(config, jmsConfig, cfConfig);
				ctx.release();
			}

			// setup defaults
			config.addAcceptorConfiguration(new TransportConfiguration(InVMAcceptorFactory.class.getName()))
					.addConnectorConfiguration("vmConnector", new TransportConfiguration(InVMConnectorFactory.class.getName()));
			cfConfig.setName("cf")
					.setConnectorNames(Collections.singletonList("vmConnector"))
					.setBindings("/cf");
			jmsConfig.getConnectionFactoryConfigurations().add(cfConfig);

			this.broker = new EmbeddedJMS();
			this.broker.setConfiguration(config).setJmsConfiguration(jmsConfig);
			this.broker.start();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	@PreDestroy
	public void destroy() {
		try {
			this.broker.stop();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	@Produces
	public EmbeddedJMS embeddedJMS() {
		return this.broker;
	}

	@Produces
	public ConnectionFactory connectionFactory() {
		return (ConnectionFactory) this.broker.lookup("/cf");
	}
}
