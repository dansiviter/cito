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
package cito.annotation;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.util.AnnotationLiteral;

/**
 * 
 * @author Daniel Siviter
 * @since v1.0 [30 Aug 2016]
 */
public class RolesAllowedLiteral extends AnnotationLiteral<RolesAllowed> implements RolesAllowed {
	private static final long serialVersionUID = 1L;

	private final String[] value;

	public RolesAllowedLiteral(String[] value) {
		this.value = value;
	}

	@Override
	public String[] value() {
		return value;
	}
}
