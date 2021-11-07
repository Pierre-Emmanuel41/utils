package fr.pederobien.utils;

public class Range<T extends Number & Comparable<T>> {

	/**
	 * Creates a range defined by a minimum value and a maximum value.
	 * 
	 * @param <T>           The type of element contained in the range.
	 * @param fromInclusive The minimum value, included.
	 * @param toInclusive   The maximum value, included.
	 * 
	 * @return The range based on the given parameter.
	 */
	public static <T extends Number & Comparable<T>> Range<T> of(T fromInclusive, T toInclusive) {
		return new Range<T>(fromInclusive, toInclusive);
	}

	private T fromInclusive, toInclusive;

	/**
	 * Creates a range with the specified minimum value and maximum value.
	 * 
	 * @param fromInclusive The minimum range value.
	 * @param toInclusive   The maximum range value.
	 */
	private Range(T fromInclusive, T toInclusive) {
		if (toInclusive.compareTo(fromInclusive) < 0)
			throw new IllegalArgumentException("The minimum of the range should be less than the maximum of the range");

		this.fromInclusive = fromInclusive;
		this.toInclusive = toInclusive;
	}

	/**
	 * @return The minimum range value.
	 */
	public Number getFrom() {
		return fromInclusive;
	}

	/**
	 * @return The maximum range value.
	 */
	public Number getTo() {
		return toInclusive;
	}

	/**
	 * Checks whether the specified number occurs within this range.
	 * 
	 * @param number The number to check.
	 * 
	 * @return True if the number is in this range, false otherwise.
	 */
	public boolean contains(T number) {
		return fromInclusive.compareTo(number) <= 0 && number.compareTo(toInclusive) <= 0;
	}

	@Override
	public String toString() {
		return String.format("[%s;%s]", fromInclusive, toInclusive);
	}
}
