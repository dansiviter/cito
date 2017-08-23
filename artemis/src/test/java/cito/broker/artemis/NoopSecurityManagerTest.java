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
package cito.broker.artemis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link NoopSecurityManager}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [15 Aug 2017]
 */
public class NoopSecurityManagerTest {
	private NoopSecurityManager securityManager;

	@Before
	public void before() {
		this.securityManager = new NoopSecurityManager();
	}

	@Test
	public void validateUser() {
		assertTrue(this.securityManager.validateUser(null, null));
	}

	@Test
	public void validateUserAndRole() {
		assertTrue(this.securityManager.validateUserAndRole(null, null, null, null));
	}

	@Test
	public void validateUser_connection() {
		final String expected = "user";
		assertEquals(expected, this.securityManager.validateUser(expected, null, null));
	}

	@Test
	public void validateUser_connection_null() {
		assertEquals("UNKNOWN", this.securityManager.validateUser(null, null, null));
	}

	@Test
	public void validateUserAndRole_connection() {
		final String expected = "user";
		assertEquals(expected, this.securityManager.validateUserAndRole(expected, null, null, null, null, null));
	}

	@Test
	public void validateUserAndRole_connection_null() {
		assertEquals("UNKNOWN", this.securityManager.validateUserAndRole(null, null, null, null, null, null));
	}
}
