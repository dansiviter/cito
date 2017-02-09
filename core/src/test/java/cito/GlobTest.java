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

import static org.junit.Assert.*;

import org.junit.Test;

import cito.Glob;

/**
 * Unit tests for {@link Glob}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [22 Sep 2016]
 */
public class GlobTest {
	@Test
	public void go() {
		assertTrue(Glob.matches("/foo/bar/", "/foo/bar/"));
		assertFalse(Glob.matches("/foo/bar/", "/foo/bar"));
		assertFalse(Glob.matches("/foo/bar/", "/foo/bar/blagh"));
		assertTrue(Glob.matches("/foo/bar/*", "/foo/bar/blagh"));
		assertTrue(Glob.matches("*/bar/*", "/foo/bar/blagh"));
		assertFalse(Glob.matches("/foo/bar/?", "/foo/bar/blagh"));
		assertTrue(Glob.matches("/foo/bar/?", "/foo/bar/b"));
		assertTrue(Glob.matches("/foo/bar/?/blagh", "/foo/bar/b/blagh"));
		assertTrue(Glob.matches("?/foo/bar/", "b/foo/bar/"));
		assertFalse(Glob.matches("?/foo/bar/", "bb/foo/bar/"));
		assertFalse(Glob.matches("/foo/bar/??/blagh", "/foo/bar/b/blagh"));
		assertTrue(Glob.matches("/foo/bar/??/blagh", "/foo/bar/bl/blagh"));
		assertFalse(Glob.matches("/foo/bar/??/blagh", "/foo/bar/b/blagh"));
		assertTrue(Glob.matches("/foo/bar/??/blagh", "/foo/bar/bl/blagh"));

		assertTrue(Glob.matches("/foo.bar/", "/foo.bar/"));
		assertFalse(Glob.matches("/foo.bar/", "/foo.bar"));
		assertFalse(Glob.matches("/foo.bar/", "/foo.bar/blagh"));
		assertTrue(Glob.matches("/foo.bar/*", "/foo.bar/blagh"));
		assertTrue(Glob.matches("*.bar/*", "/foo.bar/blagh"));
		assertFalse(Glob.matches("/foo.bar/?", "/foo.bar/blagh"));
		assertTrue(Glob.matches("/foo.bar/?", "/foo.bar/b"));
		assertTrue(Glob.matches("/foo.bar/?/blagh", "/foo.bar/b/blagh"));
		assertTrue(Glob.matches("?/foo.bar/", "b/foo.bar/"));
		assertFalse(Glob.matches("?/foo.bar/", "bb/foo.bar/"));
		assertFalse(Glob.matches("/foo.bar/??/blagh", "/foo.bar/b/blagh"));
		assertTrue(Glob.matches("/foo.bar/??/blagh", "/foo.bar/bl/blagh"));
		assertFalse(Glob.matches("/foo.bar/??/blagh", "/foo.bar/b/blagh"));
		assertTrue(Glob.matches("/foo.bar/??/blagh", "/foo.bar/bl/blagh"));
	}

	@Test
	public void capture() {
		assertTrue(Glob.matches("/foo.bar/{hello}/blagh", "/foo.bar/hello/blagh"));
	}

	@Test
	public void capture_hypen() {
		assertTrue(Glob.matches("/foo.bar/{hello}/blagh", "/foo.bar/hello-world/blagh"));
	}
}
