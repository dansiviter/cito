package cito.broker.artemis;

import static java.util.Collections.emptySet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

import org.apache.deltaspike.core.api.literal.AnyLiteral;
import org.apache.deltaspike.core.api.literal.DefaultLiteral;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [2 Feb 2017]
 */
public class BrokerConfigBean implements Bean<BrokerConfig> {

	@Override
	public Class<?> getBeanClass() {
		return DefaultBrokerConfig.class;
	}

	@Override
	public Set<InjectionPoint> getInjectionPoints() {
		return emptySet();
	}

	@Override
	public boolean isNullable() {
		return false;
	}

	@Override
	public BrokerConfig create(CreationalContext<BrokerConfig> creationalContext) {
		return new DefaultBrokerConfig();
	}

	@Override
	public void destroy(BrokerConfig configuration, CreationalContext<BrokerConfig> creationalContext) { }

	@Override
	public Set<Type> getTypes() {
		Set<Type> types = new HashSet<>();
		types.add(DefaultBrokerConfig.class);
		types.add(BrokerConfig.class);
		return types;
	}

	@Override
	public Set<Annotation> getQualifiers() {
		Set<Annotation> qualifiers = new HashSet<>();
		qualifiers.add(new AnyLiteral());
		qualifiers.add(new DefaultLiteral());
		return qualifiers;
	}

	@Override
	public Class<? extends Annotation> getScope() {
		return ApplicationScoped.class;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public Set<Class<? extends Annotation>> getStereotypes() {
		return emptySet();
	}

	@Override
	public boolean isAlternative() {
		return false;
	}
}