package cito.util;

import static java.lang.Integer.toHexString;
import static java.util.Objects.isNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Super simple builder for creating {@link Object#toString()} values for objects. The results of this should only be
 * used for debugging purposes. It's very wasteful of system resources to create {@link String}s unnecessarily as it
 * could lead to garbage collection churn caused by excessive heap utilisation.
 * 
 * @author Daniel Siviter
 * @since v1.0 [7 Jan 2018]
 */
public class ToStringBuilder {
	private static final CharSequence NULL = "null";
	private final List<Holder> values = new LinkedList<>();
	private final Object obj;
	private final CharSequence nullValue;

	private ToStringBuilder(@Nonnull Object obj, @Nonnull CharSequence nullValue) {
		this.obj = obj;
		this.nullValue = nullValue;
	}

	public ToStringBuilder append(CharSequence name, CharSequence value) {
		values.add(new Holder(name, value));
		return this;
	}

	public ToStringBuilder append(CharSequence name, Object value) {
		return append(name, Objects.toString(value));
	}

	public ToStringBuilder append(CharSequence name, boolean value) {
		return append(name, Boolean.toString(value));
	}

	public ToStringBuilder append(CharSequence name, byte value) {
		return append(name, Byte.toString(value));
	}

	public ToStringBuilder append(CharSequence name, short value) {
		return append(name, Short.toString(value));
	}

	public ToStringBuilder append(CharSequence name, int value) {
		return append(name, Integer.toString(value));
	}

	public ToStringBuilder append(CharSequence name, long value) {
		return append(name, Long.toString(value));
	}

	public ToStringBuilder append(CharSequence name, float value) {
		return append(name, Float.toString(value));
	}

	public ToStringBuilder append(CharSequence name, double value) {
		return append(name, Double.toString(value));
	}

	public ToStringBuilder append(CharSequence name, char value) {
		return append(name, Character.toString(value));
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(32)
				.append(this.obj.getClass().getName())
				.append('@')
				.append(toHexString(this.obj.hashCode()));

		if (!this.values.isEmpty()) {
			builder.append('[');
			for (Holder holder : this.values) {
				if (!isNull(holder.name)) {
					builder.append(holder.name).append('=');
				}
				builder.append(isNull(holder.value) ? this.nullValue : holder.value).append(',');
			}
			builder.setLength(builder.length() - 1);
			builder.append(']');
		}
		return builder.toString();
	}


	// --- Static Methods ---

	/**
	 * Create new builder.
	 * 
	 * @param obj the source object
	 * @return a new instance.
	 */
	public static ToStringBuilder create(@Nonnull Object obj) {
		return create(obj, NULL);
	}

	/**
	 * Create new builder.
	 * 
	 * @param obj the source object.
	 * @param nullValue the value to append for {@code null} values.
	 * @return a new instance.
	 */
	public static ToStringBuilder create(@Nonnull Object obj, @Nonnull CharSequence nullValue) {
		return new ToStringBuilder(obj, nullValue);
	}


	// --- Inner Classes ---

	/**
	 * 
	 * @author Daniel Siviter
	 * @since v1.0 [7 Jan 2018]
	 *
	 */
	private static class Holder {
		private final CharSequence name;
		private final CharSequence value;

		Holder(CharSequence name, CharSequence value) {
			this.name = name;
			this.value = value;
		}
	}
}
