package fr.pederobien.utils;

import java.util.Objects;

public class AsyncConsole {
	private static final BlockingQueueTask<Object> TASK;

	static {
		TASK = new BlockingQueueTask<>("AsyncConsole", object -> internalPrint(object));
		TASK.start();
	}

	/**
	 * Print the given object in the console of the system.
	 * 
	 * @param object The object to print.
	 * 
	 * @throws NullPointerException If the given object is null.
	 */
	public static void print(Object object) {
		Objects.requireNonNull(object);
		TASK.add(object);
	}

	/**
	 * Print the given object in the console of the system and then print a new line.
	 * 
	 * @param object The object to print.
	 * 
	 * @throws NullPointerException If the given object is null.
	 */
	public static void println(Object object) {
		print(object);
		print("\r\n");
	}

	/**
	 * Print the result of {@link String#format(String, Object...)}
	 * 
	 * @param format A formatter string.
	 * @param args   Arguments called according to the formatter.
	 * 
	 * @throws NullPointerException If the format is null.
	 */
	public static void print(String format, Object... args) {
		Objects.requireNonNull(format);
		print(String.format(format, args));
	}

	/**
	 * Print the result of {@link String#format(String, Object...)} and then print a new line.
	 * 
	 * @param format A formatter string.
	 * @param args   Arguments called according to the formatter.
	 * 
	 * @throws NullPointerException If the format is null.
	 */
	public static void println(String format, Object... args) {
		print(format, args);
		print("\r\n");
	}

	private static void internalPrint(Object object) {
		System.out.println(object);
	}
}
