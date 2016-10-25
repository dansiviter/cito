package cito;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [15 Jul 2016]
 */
public class LogProvider {
	/**
	 * 
	 * @param ip
	 * @return
	 */
	@Produces @Dependent
	public Logger logger(InjectionPoint ip) {
		return LogManager.getLogger(ip.getMember().getDeclaringClass());
	}
}
