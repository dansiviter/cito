package cito.stomp;

import static org.junit.Assert.*;

import org.junit.Test;

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
}
