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

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;

/**
 * Encapsulates the validation logic to verify the structure of a object. This will extract the
 * {@link javax.validation.Validator} from the CDI context.
 * 
 * @author Daniel Siviter
 * @since v1.0 [20 Nov 2017]
 */
@ApplicationScoped
public class Validator {
	@Inject
	private Logger log;
	@Inject
	private Instance<javax.validation.Validator> delegate;

	private boolean ignore;

	/**
	 * 
	 * @param object
	 * @param groups
	 * @throws ConstraintViolationException
	 */
	public <T> void validate(T object, Class<?>... groups) throws ConstraintViolationException {
		if (this.ignore) {
			return;
		}

		if (this.delegate.isUnsatisfied()) {
			this.log.warn("No validator found! Validation not possible and therefore subsequently ignored.");
			this.ignore = true;
			return;
		}

		final javax.validation.Validator validator = this.delegate.get();
		this.log.info("Validating object. [type={},validator={}]", object.getClass(), validator.getClass());
		final Set<ConstraintViolation<T>> violations = validator.validate(object, groups);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
	}
}
