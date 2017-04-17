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

import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

/**
 * Unit test {@link QuietClosable}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [16 Apr 2017]
 */
public class QuietClosableTest {
	@Test
	public void noop() {
		final AtomicBoolean b = new AtomicBoolean();

		try (QuietClosable closable = new QuietClosable() { public void close() { b.set(true);} }) { }

		assertTrue(b.get());
	}
}
