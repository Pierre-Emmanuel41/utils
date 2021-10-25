package fr.pederobien.utils;

import java.time.LocalTime;

public class AsyncConsole {
	private static final BlockingQueueTask<Display> TASK;
	private static final String NEW_LINE_FORMATTER = "%s\r\n";
	private static final String TIME_STAMP_FORMATTER = "[%s] %s";

	static {
		TASK = new BlockingQueueTask<>("AsyncConsole", display -> internalPrint(display));
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
		TASK.add(new Display(object, false));
	}

	/**
	 * Print the given object in the console of the system and the time at which the message has been registered.
	 * 
	 * @param object The object to print.
	 * 
	 * @throws NullPointerException If the given object is null.
	 */
	public static void printWithTimeStamp(Object object) {
		TASK.add(new Display(object, true));
	}

	/**
	 * Print the given object in the console of the system and then print a new line.
	 * 
	 * @param object The object to print.
	 * 
	 * @throws NullPointerException If the given object is null.
	 */
	public static void println(Object object) {
		TASK.add(new Display(String.format(NEW_LINE_FORMATTER, object), false));
	}

	/**
	 * Print the given object in the console of the system and the time at which the message has been registered, then print a new
	 * line.
	 * 
	 * @param object    The object to print.
	 * @param timeStamp True in order to add the time at which the message has been registered.
	 * 
	 * @throws NullPointerException If the given object is null.
	 */
	public static void printlnWithTimeStamp(Object object) {
		TASK.add(new Display(String.format(NEW_LINE_FORMATTER, object), true));
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
		print(String.format(format, args));
	}

	/**
	 * Print the result of {@link String#format(String, Object...)} and the time at which the message has been registered.
	 * 
	 * @param format A formatter string.
	 * @param args   Arguments called according to the formatter.
	 * 
	 * @throws NullPointerException If the format is null.
	 */
	public static void printWithTimeStamp(String format, Object... args) {
		printWithTimeStamp(String.format(format, args));
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
		println(String.format(format, args));
	}

	/**
	 * Print the result of {@link String#format(String, Object...)} and the time at which the message has been registered, then print
	 * a new line.
	 * 
	 * @param format A formatter string.
	 * @param args   Arguments called according to the formatter.
	 * 
	 * @throws NullPointerException If the format is null.
	 */
	public static void printlnWithTimeStamp(String format, Object... args) {
		printlnWithTimeStamp(String.format(format, args));
	}

	private static void internalPrint(Display display) {
		System.out.print(display.isTimeStamp() ? String.format(TIME_STAMP_FORMATTER, display.getTime(), display.getToDisplay()) : display.getToDisplay());
	}

	private static class Display {
		private Object toDisplay;
		private LocalTime time;
		private boolean timeStamp;

		private Display(Object toDisplay, boolean timeStamp) {
			this.toDisplay = toDisplay;
			this.timeStamp = timeStamp;
			if (timeStamp)
				time = LocalTime.now();
		}

		public Object getToDisplay() {
			return toDisplay;
		}

		public boolean isTimeStamp() {
			return timeStamp;
		}

		public LocalTime getTime() {
			return time;
		}
	}
}
