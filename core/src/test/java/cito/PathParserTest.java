package cito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cito.PathParser.Result;

/**
 * JUnit test for {@link PathParser}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [17 Jan 2017]
 */
public class PathParserTest {
	@Test
	public void parse_forwardSlash() {
		final PathParser parser = new PathParser("/queue/{param1}/segment/{param2}");
		final Result result = parser.parse("/queue/first/segment/second");
		assertTrue(result.isSuccess());
		assertEquals("first", result.get("param1"));
		assertEquals("second", result.get("param2"));
	}

	@Test
	public void parse_period() {
		final PathParser parser = new PathParser("/queue/{param1}.segment.{param2}");
		final Result result = parser.parse("/queue/first.segment.second");
		assertTrue(result.isSuccess());
		assertEquals("first", result.get("param1"));
		assertEquals("second", result.get("param2"));
	}

	@Test
	public void parse_endSeparator() {
		final PathParser parser = new PathParser("/queue/{param1}/segment/{param2}/");
		final Result result = parser.parse("/queue/first/segment/second");
		assertTrue(result.isSuccess());
		assertEquals("first", result.get("param1"));
		assertEquals("second", result.get("param2"));
	}

	@Test
	public void parse_noPrefix() {
		final PathParser parser = new PathParser("{param1}/segment/{param2}");
		final Result result = parser.parse("first/segment/second");
		assertTrue(result.isSuccess());
		assertEquals("first", result.get("param1"));
		assertEquals("second", result.get("param2"));
	}

	@Test
	public void parse_invalidSegments() {
		final PathParser parser = new PathParser("{param1}/segment/{param2}");
		assertFalse(parser.parse("/queue/first/segment/second").isSuccess());
	}

	@Test
	public void parse_nonMatchingSegments() {
		final PathParser parser = new PathParser("/queue/{param1}/segment");
		assertFalse(parser.parse("/queue/first/another").isSuccess());
	}
}
