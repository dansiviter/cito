package cito;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * JUnit test for {@link PathParser}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [17 Jan 2017]
 */
public class PathParserTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void parse_forwardSlash() {
		final PathParser parser = new PathParser("/queue/{param1}/segment/{param2}");
		final Map<String, String> results = parser.parse("/queue/first/segment/second");
		assertEquals("first", results.get("param1"));
		assertEquals("second", results.get("param2"));
	}

	@Test
	public void parse_period() {
		final PathParser parser = new PathParser("/queue/{param1}.segment.{param2}");
		final Map<String, String> results = parser.parse("/queue/first.segment.second");
		assertEquals("first", results.get("param1"));
		assertEquals("second", results.get("param2"));
	}

	@Test
	public void parse_endSeparator() {
		final PathParser parser = new PathParser("/queue/{param1}/segment/{param2}/");
		final Map<String, String> results = parser.parse("/queue/first/segment/second");
		assertEquals("first", results.get("param1"));
		assertEquals("second", results.get("param2"));
	}

	@Test
	public void parse_noPrefix() {
		final PathParser parser = new PathParser("{param1}/segment/{param2}");
		final Map<String, String> results = parser.parse("first/segment/second");
		assertEquals("first", results.get("param1"));
		assertEquals("second", results.get("param2"));
	}

	@Test
	public void parse_invalidSegments() {
		this.exception.expect(IllegalArgumentException.class);
		this.exception.expectMessage("Different number of segments! [pattern={param1}/segment/{param2},path=/queue/first/segment/second]");
		final PathParser parser = new PathParser("{param1}/segment/{param2}");
		parser.parse("/queue/first/segment/second");
	}

	@Test
	public void parse_nonMatchingSegments() {
		this.exception.expect(IllegalArgumentException.class);
		this.exception.expectMessage("Unnamed segment does not match pattern! [pattern=/queue/{param1}/segment,path=/queue/first/another,segment=3]");
		final PathParser parser = new PathParser("/queue/{param1}/segment");
		parser.parse("/queue/first/another");
	}
}
