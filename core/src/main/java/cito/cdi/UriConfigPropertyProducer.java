package cito.cdi;

import java.net.URI;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.deltaspike.core.spi.config.BaseConfigPropertyProducer;

/**
 * Produces {@link URI} configuation properties.
 * 
 * @author Daniel Siviter
 * @since v1.0 [19 Jul 2016]
 */
public class UriConfigPropertyProducer extends BaseConfigPropertyProducer {
	@Produces
	@Dependent
	@ConfigProperty(name = "unused")
	public URI produceUri(InjectionPoint injectionPoint) {
		String configuredValue = getStringPropertyValue(injectionPoint);
		return URI.create(configuredValue);
	}
}