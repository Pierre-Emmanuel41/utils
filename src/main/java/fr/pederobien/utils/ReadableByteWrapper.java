package fr.pederobien.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.StringJoiner;

public class ReadableByteWrapper {
	private ByteBuffer buffer;
	private int position;

	/**
	 * Create a new readable wrapper based on the given byte array. A readable wrapper contains an internal cursor whose the value is
	 * updated according to the value read.
	 * 
	 * @param data       The byte array to wrap.
	 * @param endianness The byte order to use.
	 * 
	 * @return A byte wrapper.
	 */
	private ReadableByteWrapper(byte[] data, ByteOrder endianness) {
		buffer = ByteBuffer.wrap(data).order(endianness);
		position = 0;
	}

	/**
	 * Create a new readable wrapper based on the given byte array. A readable wrapper contains an internal cursor whose the value is
	 * updated according to the value read.
	 * 
	 * @param buffer The byte array to wrap.
	 * 
	 * @return A byte wrapper.
	 */
	public static ReadableByteWrapper wrap(byte[] buffer) {
		return new ReadableByteWrapper(buffer, ByteOrder.BIG_ENDIAN);
	}

	/**
	 * Create a new readable wrapper based on the given byte array. A readable wrapper contains an internal cursor whose the value is
	 * updated according to the value read.
	 * 
	 * @param buffer     The byte array to wrap.
	 * @param endianness The byte order to use.
	 * 
	 * @return A byte wrapper.
	 */
	public static ReadableByteWrapper wrap(byte[] buffer, ByteOrder endianness) {
		return new ReadableByteWrapper(buffer, endianness);
	}

	/**
	 * Reads the next byte and increment by one the current position by one.
	 *
	 * @return The byte at the current position.
	 */
	public byte next() {
		byte value = buffer.get(position);
		position += 1;
		return value;
	}

	/**
	 * Reads the next n bytes, with n equals length, and increment by n the current position by length. If length is -1, read until
	 * the end of the underlying bytes array.
	 *
	 * @param length The number of bytes to read.
	 * 
	 * @return The extracted bytes array from the current position.
	 */
	public byte[] next(int length) {
		if (length < 0)
			length = get().length - position;
		else {
			int end = position + length;
			if (end > get().length)
				throw new IndexOutOfBoundsException(end);
		}

		byte[] data = new byte[length];
		System.arraycopy(get(), position, data, 0, length);
		position += length;
		return data;
	}

	/**
	 * Reads the next two bytes, composing them into a short value according to the current byte order and increment the current
	 * position by two.
	 * 
	 * @return The short value at the current position.
	 */
	public short nextShort() {
		short value = buffer.getShort(position);
		position += 2;
		return value;
	}

	/**
	 * Reads the next four bytes, composing them into an integer value according to the current byte order and increment the current
	 * position by four.
	 *
	 * @return The integer value at the current position.
	 */
	public int nextInt() {
		int value = buffer.getInt(position);
		position += 4;
		return value;
	}

	/**
	 * Reads the next height bytes, composing them into a long value according to the current byte order and increment the current
	 * position by height.
	 *
	 * @return The long value at the current position.
	 */
	public long nextLong() {
		long value = buffer.getLong(position);
		position += 8;
		return value;
	}

	/**
	 * Reads the next four bytes, composing them into a float value according to the current byte order and increment the current
	 * position by four.
	 *
	 * @return The float value at the current position.
	 */
	public float nextFloat() {
		float value = buffer.getFloat(position);
		position += 4;
		return value;
	}

	/**
	 * Reads the next height bytes, composing them into a double value according to the current byte order and increment the current
	 * position by height.
	 *
	 * @return The double value at the current position.
	 */
	public double nextDouble() {
		double value = buffer.getDouble(position);
		position += 8;
		return value;
	}

	/**
	 * Read the next n bytes, with n equals length, at the given index and creates a string based on the corresponding bytes array,
	 * and then increment the current position by n.
	 * 
	 * @param length The number of bytes to read.
	 * 
	 * @return A string.
	 */
	public String nextString(int length) {
		return new String(next(length));
	}

	/**
	 * @return The buffer associated to this wrapper.
	 */
	public byte[] get() {
		return buffer.array();
	}

	/**
	 * Set the current position of this readable wrapper.
	 * 
	 * @param position The new position of this wrapper.
	 * 
	 * @throws IndexOutOfBoundsException If position is out of range [0, length]
	 */
	public void setPosition(int position) {
		if (position < 0 || position > get().length)
			throw new IndexOutOfBoundsException(position);

		this.position = position;
	}

	/**
	 * @return The current position from where a new value can be read.
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * Search in the underlying buffer if the given pattern is present.
	 * 
	 * @param pattern The pattern to look for.
	 * 
	 * @return -1 if the pattern is not present, or the index of the first occurrence of the pattern.
	 */
	public int nextIndexOf(byte[] pattern) {
		int index = -1;
		byte[] buffer = get();

		// Iterating over the buffer
		for (int i = position; i <= buffer.length - pattern.length; i++) {
			boolean match = true;

			// Iterating over the pattern
			for (int j = 0; (j < pattern.length) && match; j++)
				match = buffer[i + j] == pattern[j];

			if (match) {
				position += pattern.length;
				return i;
			}

			position++;
		}

		return index;
	}

	/**
	 * Search in the underlying buffer if the given pattern is present.
	 * 
	 * @param position The index to start from.
	 * @param pattern  The pattern to look for.
	 * 
	 * @return -1 if the pattern is not present, or the index of the first occurrence of the pattern.
	 */
	public int nextIndexOf(int position, byte[] pattern) {
		setPosition(position);
		return nextIndexOf(pattern);
	}

	/**
	 * @return Creates a String based on this buffer.
	 */
	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "[", "]");
		for (byte b : get())
			joiner.add("" + b);
		return joiner.toString();
	}
}
