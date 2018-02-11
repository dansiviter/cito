package cito;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for {@link Strings}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [11 Feb 2018]
 */
public class StringsTest {
	@Test
	public void isEmpty() {
		assertTrue(Strings.isEmpty(""));
		assertTrue(Strings.isEmpty(null));
		assertTrue(Strings.isEmpty(""));
		assertFalse(Strings.isEmpty(" "));
		assertFalse(Strings.isEmpty("abcd"));
	}

	@Test
	public void isBlank() {
		assertTrue(Strings.isBlank(""));
		assertTrue(Strings.isBlank(null));
		assertTrue(Strings.isBlank(""));
		assertTrue(Strings.isBlank(" "));
		assertFalse(Strings.isBlank("abcd"));
	}
}
