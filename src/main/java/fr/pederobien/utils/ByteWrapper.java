package fr.pederobien.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.StringJoiner;

public class ByteWrapper {
	private byte[] buffer;
	private ByteBuffer byteBuffer;

	/**
	 * Creates a byte wrapper in order to store byte representation of different data.
	 * 
	 * @param buffer     The underlying array.
	 * @param endianness The byte order to use to store data.
	 */
	private ByteWrapper(byte[] buffer, ByteOrder endianness) {
		this.buffer = buffer;
		byteBuffer = ByteBuffer.wrap(buffer).order(endianness);
	}

	/**
	 * Create a new wrapper based on the the given byte array. The default byte order is Big-Endian.
	 * 
	 * @param buffer The byte array to wrap.
	 * 
	 * @return A byte wrapper.
	 */
	public static ByteWrapper wrap(byte[] buffer) {
		return new ByteWrapper(buffer, ByteOrder.BIG_ENDIAN);
	}

	/**
	 * Create a new wrapper based on the the given byte array.
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
		return wrap(new byte[0]);
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
		return internalPut(ByteBuffer.allocate(2).putShort(value).array());
	}

	/**
	 * Creates the bytes array associated to the given int number and concatenates the result to this buffer.
	 * 
	 * @param value The int value to add.
	 * 
	 * @return A byte wrapper.
	 */
	public ByteWrapper putInt(int value) {
		return internalPut(ByteBuffer.allocate(4).putInt(value).array());
	}

	/**
	 * Creates the bytes array associated to the given long number and concatenates the result to this buffer.
	 * 
	 * @param value The long value to add.
	 * 
	 * @return A byte wrapper.
	 */
	public ByteWrapper putLong(long value) {
		return internalPut(ByteBuffer.allocate(8).putLong(value).array());
	}

	/**
	 * Creates the bytes array associated to the given float number and concatenates the result to this buffer.
	 * 
	 * @param value The float value to add.
	 * 
	 * @return A byte wrapper.
	 */
	public ByteWrapper putFloat(float value) {
		return internalPut(ByteBuffer.allocate(4).putFloat(value).array());
	}

	/**
	 * Creates the bytes array associated to the given double number and concatenates the result to this buffer.
	 * 
	 * @param value The double value to add.
	 * 
	 * @return A byte wrapper.
	 */
	public ByteWrapper putDouble(double value) {
		return internalPut(ByteBuffer.allocate(8).putDouble(value).array());
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
		if (!specifyLength)
			return internalPut(string.getBytes());

		byte[] buffer = string.getBytes();
		putInt(buffer.length);
		return internalPut(buffer);
	}

	/**
	 * Reads the byte at the given index.
	 *
	 * @param index The index from which the byte will be read
	 *
	 * @return The byte at the given index
	 */
	public byte get(int index) {
		return byteBuffer.get(index);
	}

	/**
	 * Reads two bytes at the given index, composing them into a short value according to the current byte order.
	 * 
	 * @param index The index from which the bytes will be read.
	 *
	 * @return The short value at the given index.
	 */
	public short getShort(int index) {
		return byteBuffer.getShort(index);
	}

	/**
	 * Reads four bytes at the given index, composing them into a int value according to the current byte order.
	 *
	 * @param index The index from which the bytes will be read.
	 *
	 * @return The int value at the given index.
	 */
	public int getInt(int index) {
		return byteBuffer.getInt(index);
	}

	/**
	 * Reads height bytes at the given index, composing them into a long value according to the current byte order.
	 *
	 * @param index The index from which the bytes will be read.
	 *
	 * @return The long value at the given index.
	 */
	public long getLong(int index) {
		return byteBuffer.getLong(index);
	}

	/**
	 * Reads four bytes at the given index, composing them into a float value according to the current byte order.
	 *
	 * @param index The index from which the bytes will be read.
	 *
	 * @return The float value at the given index.
	 */
	public float getFloat(int index) {
		return byteBuffer.getFloat(index);
	}

	/**
	 * Reads height bytes at the given index, composing them into a double value according to the current byte order.
	 *
	 * @param index The index from which the bytes will be read.
	 *
	 * @return The double value at the given index.
	 */
	public double getDouble(int index) {
		return byteBuffer.getDouble(index);
	}

	/**
	 * @return Creates a String based on this buffer.
	 */
	public String getString() {
		return new String(buffer);
	}

	/**
	 * Read n bytes, with n equals length, at the given index and creates a string based on the corresponding bytes array.
	 * 
	 * @param index  The index from which the bytes will be read.
	 * @param length The number of bytes to read.
	 * 
	 * @return A string.
	 */
	public String getString(int index, int length) {
		return new String(extract(index, length));
	}
	
	/**
	 * @return A readable wrapper that wrap the underlying bytes array.
	 */
	public ReadableByteWrapper getAsReadableWrapper() {
		return ReadableByteWrapper.wrap(get());
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
	 * @param index  The index from which the bytes will be take.
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
		byteBuffer = ByteBuffer.wrap(this.buffer);
		return this;
	}

}
