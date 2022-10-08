package fr.pederobien.utils;

import java.nio.ByteOrder;
import java.util.function.Function;

public class ReadableByteWrapper {
	private ByteWrapper wrapper;
	private int position;

	/**
	 * Create a new readable wrapper based on the the given byte array. A readable wrapper contains an internal cursor whose the value
	 * is updated according to the value read.
	 * 
	 * @param buffer     The byte array to wrap.
	 * @param endianness The byte order to use.
	 * 
	 * @return A byte wrapper.
	 */
	private ReadableByteWrapper(byte[] data, ByteOrder endianness) {
		wrapper = ByteWrapper.wrap(data, endianness);
		position = 0;
	}

	/**
	 * Create a new readable wrapper based on the the given byte array. A readable wrapper contains an internal cursor whose the value
	 * is updated according to the value read.
	 * 
	 * @param buffer The byte array to wrap.
	 * 
	 * @return A byte wrapper.
	 */
	public static ReadableByteWrapper wrap(byte[] buffer) {
		return new ReadableByteWrapper(buffer, ByteOrder.BIG_ENDIAN);
	}

	/**
	 * Create a new readable wrapper based on the the given byte array. A readable wrapper contains an internal cursor whose the value
	 * is updated according to the value read.
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
		return next(wrapper -> wrapper.get(position), 1);
	}

	/**
	 * Reads the next n bytes, with n equals length, and increment by n the current position by one.
	 *
	 * @param The number of bytes to read.
	 * 
	 * @return The byte at the current position.
	 */
	public byte[] next(int length) {
		return next(wrapper -> wrapper.extract(position, length), length);
	}

	/**
	 * Reads the next two bytes, composing them into a short value according to the current byte order and increment the current
	 * position by two.
	 * 
	 * @return The short value at the current position.
	 */
	public short nextShort() {
		return next(wrapper -> wrapper.getShort(position), 2);
	}

	/**
	 * Reads the next four bytes, composing them into a integer value according to the current byte order and increment the current
	 * position by four.
	 *
	 * @return The integer value at the current position.
	 */
	public int nextInt() {
		return next(wrapper -> wrapper.getInt(position), 4);
	}

	/**
	 * Reads the next height bytes, composing them into a long value according to the current byte order and increment the current
	 * position by height.
	 *
	 * @return The long value at the current position.
	 */
	public long nextLong() {
		return next(wrapper -> wrapper.getLong(position), 8);
	}

	/**
	 * Reads the next four bytes, composing them into a float value according to the current byte order and increment the current
	 * position by four.
	 *
	 * @return The float value at the current position.
	 */
	public float nextFloat() {
		return next(wrapper -> wrapper.getFloat(position), 4);
	}

	/**
	 * Reads the next height bytes, composing them into a double value according to the current byte order and increment the current
	 * position by height.
	 *
	 * @return The double value at the current position.
	 */
	public double nextDouble() {
		return next(wrapper -> wrapper.getDouble(position), 8);
	}

	/**
	 * @return Creates a String based on this buffer.
	 */
	public String nextString() {
		return new String(wrapper.get());
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
		return next(wrapper -> wrapper.getString(position, length), length);
	}

	/**
	 * @return The buffer associated to this wrapper.
	 */
	public byte[] get() {
		return wrapper.get();
	}

	/**
	 * Set the current position of this readable wrapper.
	 * 
	 * @param position The new position of this wrapper.
	 * 
	 * @throws IndexOutOfBoundsException If position is out of range [0, length]
	 */
	public void setPosition(int position) {
		if (position < 0 || position > wrapper.get().length)
			throw new IndexOutOfBoundsException(position);

		this.position = position;
	}

	/**
	 * Read n byte in the wrapper and then increment by n the current position.
	 * 
	 * @param <T>       The type of object to read.
	 * @param function  The function that read bytes in the internal wrapper.
	 * @param increment The number of bytes to read.
	 * 
	 * @return The object associated to the read bytes.
	 */
	private <T> T next(Function<ByteWrapper, T> function, int increment) {
		T value = function.apply(wrapper);
		position += increment;
		return value;
	}
}
