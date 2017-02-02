package cito.broker.artemis;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [2 Feb 2017]
 */
public class Extension implements javax.enterprise.inject.spi.Extension {
	void afterBeanDiscovery(@Observes AfterBeanDiscovery abd) {
		abd.addBean(new BrokerConfigBean());
	}
}
