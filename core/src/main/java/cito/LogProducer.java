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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Produces {@link Logger} instances for injection.
 * 
 * @author Daniel Siviter
 * @since v1.0 [15 Jul 2016]
 */
@ApplicationScoped
public class LogProducer {
	/**
	 * 
	 * @param ip
	 * @return
	 */
	@Produces @Dependent
	public static Logger logger(InjectionPoint ip) {
		return LoggerFactory.getLogger(ip.getBean().getBeanClass());
	}
}
