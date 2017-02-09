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
	public void parse_hyphen() {
		final PathParser parser = new PathParser("/queue/{param1}");
		final Result result = parser.parse("/queue/first-segment");
		assertTrue(result.isSuccess());
		assertEquals("first-segment", result.get("param1"));
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

	@Test
	public void parse_wildcard() {
		final PathParser parser = new PathParser("{param1}/segment/{param2}/*");
		final Result result = parser.parse("first/segment/second/anything");
		assertTrue(result.isSuccess());
		assertEquals("first", result.get("param1"));
		assertEquals("second", result.get("param2"));
	}
}
