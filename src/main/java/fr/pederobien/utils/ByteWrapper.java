package fr.pederobien.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.StringJoiner;

public class ByteWrapper {
	private byte[] buffer;
	private ByteOrder endianness;

	/**
	 * Creates a byte wrapper in order to store byte representation of different data.
	 * 
	 * @param buffer     The underlying array.
	 * @param endianness The byte order to use to store data.
	 */
	private ByteWrapper(byte[] buffer, ByteOrder endianness) {
		this.buffer = buffer;
		this.endianness = endianness;
	}

	/**
	 * Create a new wrapper based on the given byte array. The default byte order is Big-Endian.
	 * 
	 * @param buffer The byte array to wrap.
	 * 
	 * @return A byte wrapper.
	 */
	public static ByteWrapper wrap(byte[] buffer) {
		return new ByteWrapper(buffer, ByteOrder.BIG_ENDIAN);
	}

	/**
	 * Create a new wrapper based on the given byte array.
	 * 
	 * @param buffer     The byte array to wrap.
	 * @param endianness The byte order to use.
	 * 
	 * @return A byte wrapper.
	 */
	public static ByteWrapper wrap(byte[] buffer, ByteOrder endianness) {
		return new ByteWrapper(buffer, endianness);
	}

	/**
	 * Creates a new wrapper based on an empty bytes array. The default byte order is Big-Endian.
	 * 
	 * @return A byte wrapper.
	 */
	public static ByteWrapper create() {
		return wrap(new byte[0]);
	}

	/**
	 * Creates a new wrapper based on an empty bytes array.
	 * 
	 * @return A byte wrapper.
	 * @param endianness The byte order to use.
	 */
	public static ByteWrapper create(ByteOrder endianness) {
		return wrap(new byte[0], endianness);
	}

	/**
	 * Creates a one length bytes array and concatenates it to this buffer;
	 * 
	 * @param b The byte to add.
	 * 
	 * @return A byte wrapper.
	 */
	public ByteWrapper put(byte b) {
		return internalPut(new byte[] { b });
	}

	/**
	 * Concatenates the given buffer at the end of this buffer.
	 * 
	 * @param buffer The buffer to add.
	 * 
	 * @return A byte wrapper
	 */
	public ByteWrapper put(byte[] buffer) {
		return put(buffer, false);
	}

	/**
	 * Concatenates the given buffer at the end of this buffer.
	 * 
	 * @param buffer        The buffer to add.
	 * @param specifyLength True if the length of the byte array should be written in this buffer before writing the given buffer.
	 * 
	 * @return A byte wrapper.
	 */
	public ByteWrapper put(byte[] buffer, boolean specifyLength) {
		if (!specifyLength)
			return internalPut(buffer);

		putInt(buffer.length);
		return internalPut(buffer);
	}

	/**
	 * Creates the bytes array associated to the given short number and concatenates the result to this buffer.
	 * 
	 * @param value The sort value to add.
	 * 
	 * @return A byte wrapper.
	 */
	public ByteWrapper putShort(short value) {
		return internalPut(allocate(2).putShort(value).array());
	}

	/**
	 * Creates the bytes array associated to the given int number and concatenates the result to this buffer.
	 * 
	 * @param value The int value to add.
	 * 
	 * @return A byte wrapper.
	 */
	public ByteWrapper putInt(int value) {
		return internalPut(allocate(4).putInt(value).array());
	}

	/**
	 * Creates the bytes array associated to the given long number and concatenates the result to this buffer.
	 * 
	 * @param value The long value to add.
	 * 
	 * @return A byte wrapper.
	 */
	public ByteWrapper putLong(long value) {
		return internalPut(allocate(8).putLong(value).array());
	}

	/**
	 * Creates the bytes array associated to the given float number and concatenates the result to this buffer.
	 * 
	 * @param value The float value to add.
	 * 
	 * @return A byte wrapper.
	 */
	public ByteWrapper putFloat(float value) {
		return internalPut(allocate(4).putFloat(value).array());
	}

	/**
	 * Creates the bytes array associated to the given double number and concatenates the result to this buffer.
	 * 
	 * @param value The double value to add.
	 * 
	 * @return A byte wrapper.
	 */
	public ByteWrapper putDouble(double value) {
		return internalPut(allocate(8).putDouble(value).array());
	}

	/**
	 * Concatenates the bytes array associated to the given string to this buffer.
	 * 
	 * @param string The string to add.
	 * 
	 * @return A byte wrapper.
	 */
	public ByteWrapper putString(String string) {
		return putString(string, false);
	}

	/**
	 * Concatenates the bytes array associated to the given string to this buffer.
	 * 
	 * @param string        The string to add.
	 * @param specifyLength True if the length of the byte array should be written in this buffer before writing the byte array
	 *                      associated to the given string.
	 * 
	 * @return A byte wrapper.
	 */
	public ByteWrapper putString(String string, boolean specifyLength) {
		return put(string.getBytes(), specifyLength);
	}

	/**
	 * @return The buffer associated to this wrapper.
	 */
	public byte[] get() {
		return buffer;
	}

	/**
	 * Takes n bytes, with n equals length, at the given index and returns the corresponding bytes array. The source array is modified
	 * such as the returned bytes array is no more contained in this wrapper.
	 * 
	 * @param index  The index from which the bytes will be taken.
	 * @param length The number of bytes to take.
	 * 
	 * @return A bytes array.
	 */
	public byte[] take(int index, int length) {
		byte[] result = new byte[length];
		byte[] intermediate = new byte[get().length - length];
		System.arraycopy(get(), index, result, 0, length);
		System.arraycopy(get(), 0, intermediate, 0, index);
		System.arraycopy(get(), index + length, intermediate, index, get().length - (index + length));
		buffer = intermediate;
		return result;
	}

	/**
	 * Read n bytes, with n equals length, at the given index and returns the corresponding bytes array.
	 * 
	 * @param index  The index from which the bytes will be read.
	 * @param length The number of bytes to read.
	 * 
	 * @return A bytes array.
	 */
	public byte[] extract(int index, int length) {
		byte[] intermediate = new byte[length];
		System.arraycopy(buffer, index, intermediate, 0, length);
		return intermediate;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "[", "]");
		for (byte b : get())
			joiner.add("" + b);
		return joiner.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof ByteWrapper))
			return false;

		ByteWrapper other = (ByteWrapper) obj;
		return toString().compareTo(other.toString()) == 0;
	}

	private ByteWrapper internalPut(byte[] buffer) {
		byte[] intermediate = new byte[get().length + buffer.length];
		System.arraycopy(get(), 0, intermediate, 0, get().length);
		System.arraycopy(buffer, 0, intermediate, get().length, buffer.length);
		this.buffer = intermediate;
		return this;
	}

	/**
	 * Allocate n bytes in a new ByteBuffer, with endianness specified at instantiation.
	 * 
	 * @param capacity The number of byte to allocate.
	 * @return A byteBuffer with n byte allocated and same endianness as the main byte buffer.
	 */
	private ByteBuffer allocate(int capacity) {
		return ByteBuffer.allocate(capacity).order(endianness);
	}

}
