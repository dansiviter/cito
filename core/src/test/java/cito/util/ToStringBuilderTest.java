package cito.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for {@link ToStringBuilder}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [7 Jan 2018]
 *
 */
public class ToStringBuilderTest {
	private ToStringBuilder createBasic() {
		final Object test = new TestObject();
		return ToStringBuilder.create(test);
	}

	@Test
	public void toString_empty() {
		final String expected = "cito.util.ToStringBuilderTest$TestObject@7b";
		final String actual = createBasic().toString();
		assertEquals(expected, actual);
	}

	@Test
	public void toString_nullName() {
		final String expected = "cito.util.ToStringBuilderTest$TestObject@7b[value]";
		final String actual = createBasic().append(null, "value").toString();
		assertEquals(expected, actual);
	}

	@Test
	public void toString_nullValue() {
		final String expected = "cito.util.ToStringBuilderTest$TestObject@7b[name=null]";
		final String actual = createBasic().append("name", null).toString();
		assertEquals(expected, actual);
	}

	@Test
	public void toString_nullValue_alternate() {
		final String expected = "cito.util.ToStringBuilderTest$TestObject@7b[name=Me? Null?!]";
		final String actual = ToStringBuilder.create(new TestObject(), "Me? Null?!").append("name", null).toString();
		assertEquals(expected, actual);
	}

	@Test
	public void toString_multiple() {
		final String expected = "cito.util.ToStringBuilderTest$TestObject@7b[value1=1,value2=hi]";
		final String actual = createBasic().append("value1", 1).append("value2", "hi").toString();
		assertEquals(expected, actual);
	}

	@Test
	public void toString_stackoverflow() {
		final String expected = "cito.util.ToStringBuilderTest$SackOverflowTestObject@7b";
		final String actual = new SackOverflowTestObject().toString();
		assertEquals(expected, actual);
	}

	@Test
	public void toString_boolean() {
		final String expected = "cito.util.ToStringBuilderTest$TestObject@7b[boolean=true]";
		final String actual = createBasic().append("boolean", true).toString();
		assertEquals(expected, actual);
	}

	@Test
	public void toString_byte() {
		final String expected = "cito.util.ToStringBuilderTest$TestObject@7b[byte=5]";
		final String actual = createBasic().append("byte", (byte) 5).toString();
		assertEquals(expected, actual);
	}

	@Test
	public void toString_short() {
		final String expected = "cito.util.ToStringBuilderTest$TestObject@7b[short=5]";
		final String actual = createBasic().append("short", (short) 5).toString();
		assertEquals(expected, actual);
	}

	@Test
	public void toString_int() {
		final String expected = "cito.util.ToStringBuilderTest$TestObject@7b[int=5]";
		final String actual = createBasic().append("int", 5).toString();
		assertEquals(expected, actual);
	}

	@Test
	public void toString_long() {
		final String expected = "cito.util.ToStringBuilderTest$TestObject@7b[long=5]";
		final String actual = createBasic().append("long", 5L).toString();
		assertEquals(expected, actual);
	}

	@Test
	public void toString_float() {
		final String expected = "cito.util.ToStringBuilderTest$TestObject@7b[float=1.1]";
		final String actual = createBasic().append("float", 1.1).toString();
		assertEquals(expected, actual);
	}

	@Test
	public void toString_double() {
		final String expected = "cito.util.ToStringBuilderTest$TestObject@7b[double=1.1]";
		final String actual = createBasic().append("double", 1.1D).toString();
		assertEquals(expected, actual);
	}


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [7 Jan 2018]
	 *
	 */
	private static class TestObject {
		@Override
		public int hashCode() {
			return 123;
		}
	}

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [7 Jan 2018]
	 *
	 */
	private static class SackOverflowTestObject extends TestObject {
		@Override
		public String toString() {
			return ToStringBuilder.create(this).toString();
		}
	}
}
