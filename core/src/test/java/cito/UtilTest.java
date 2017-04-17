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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Unit tests for {@link Util}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [16 Apr 2017]
 */
public class UtilTest {
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void getFirst() {
		final String[] array = { "first", "second" };
		assertEquals("first", Util.getFirst(array));
	}

	@Test
	public void getFirst_collection() {
		final List<String> collection = Arrays.asList(new String[] { "first", "second" });;
		assertEquals("first", Util.getFirst(collection));
	}

	@Test
	public void isEmpty() {
		assertTrue(Util.isEmpty(""));
		assertFalse(Util.isEmpty(null));
		assertFalse(Util.isEmpty(" "));
		assertFalse(Util.isEmpty("-"));
	}

	@Test
	public void isNullOrEmpty() {
		assertTrue(Util.isNullOrEmpty(""));
		assertTrue(Util.isNullOrEmpty(null));
		assertFalse(Util.isNullOrEmpty(" "));
		assertFalse(Util.isNullOrEmpty("-"));
	}

	@Test
	public void requireNonEmpty() {
		assertNotNull(Util.requireNonEmpty(Collections.singleton("hello")));
	}

	@Test
	public void requireNonEmpty_empty() {
		this.expectedException.expect(IllegalArgumentException.class);
		this.expectedException.expectMessage("Collection is empty!");

		Util.requireNonEmpty(Collections.emptyList());
	}

	@Test
	public void requireNonEmpty_null() {
		this.expectedException.expect(NullPointerException.class);

		Util.requireNonEmpty(null);
	}
}
