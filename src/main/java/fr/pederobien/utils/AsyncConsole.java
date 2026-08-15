package fr.pederobien.utils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class AsyncConsole {

	private static class AsyncConsoleHolder {
		private static final AsyncConsole CONSOLE;

		static {
			CONSOLE = new AsyncConsole();
		}
	}

	/**
	 * Set if a new line shall be printed after printing a text.
	 * 
	 * @param newLine True to print a new line, false otherwise.
	 */
	public static final void setNewLine(boolean newLine) {
		get().setNewLine0(newLine);
	}

	/**
	 * Set if the time stamp shall be printed before printing a text.
	 * 
	 * @param timeStamp True to print time stamp, false otherwise.
	 */
	public static final void setTimeStamp(boolean timestamp) {
		get().setTimeStamp0(timestamp);
	}

	/**
	 * Set if the color shall be used when printing a text.
	 * 
	 * @param colorized True to use the color, false otherwise.
	 */
	public static final void setColorized(boolean colorized) {
		get().setColorized0(colorized);
	}

	/**
	 * Format the given format and args array before printing the result
	 * asynchronously.
	 * 
	 * @param color  The color to use to print the input text.
	 * @param format The string format.
	 * @param args   The format's arguments.
	 */
	public static final void print(EConsoleColor color, String format, Object... args) {
		get().print0(color, format, args);
	}

	/**
	 * Format the given format and args array before printing the result
	 * asynchronously.
	 * 
	 * @param format The string format.
	 * @param args   The format's arguments.
	 */
	public static final void print(String format, Object... args) {
		print(EConsoleColor.RESET, format, args);
	}

	private final List<String> list;
	private final Lock lock;
	private final Condition notEmpty;
	private final Thread asyncThread;
	private boolean newLine;
	private boolean timestamp;
	private boolean colorized;

	private AsyncConsole() {
		list = new ArrayList<String>();
		lock = new ReentrantLock(true);
		notEmpty = lock.newCondition();
		asyncThread = new Thread(this::printAsync, "AsyncConsole");
		asyncThread.setDaemon(true);
		asyncThread.start();
	}

	private static AsyncConsole get() {
		return AsyncConsoleHolder.CONSOLE;
	}

	/**
	 * Set if a new line shall be printed after printing a text.
	 * 
	 * @param newLine True to print a new line, false otherwise.
	 */
	private void setNewLine0(boolean newLine) {
		this.newLine = newLine;
	}

	/**
	 * Set if the time stamp shall be printed before printing a text.
	 * 
	 * @param timeStamp True to print time stamp, false otherwise.
	 */
	private void setTimeStamp0(boolean timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Set if the color shall be used when printing a text.
	 * 
	 * @param colorized True to use the color, false otherwise.
	 */
	private void setColorized0(boolean colorized) {
		this.colorized = colorized;
	}

	/**
	 * Print the given text asynchronously. The time stamp and the new line is added
	 * if enabled.
	 * 
	 * @param text The text to print.
	 */
	private void print0(EConsoleColor color, String text) {
		if (timestamp) {
			String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss:SSSS"));
			text = String.format("[%s] %s", time, text);
		}

		if (newLine)
			text = String.format("%s\n", text);

		lock.lock();
		try {
			list.add(colorized ? color.color(text) : text);
			notEmpty.signal();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Format the given format and args array before printing the result
	 * asynchronously.
	 * 
	 * @param color  The color to use to print the input text.
	 * @param format The string format.
	 * @param args   The format's arguments.
	 */
	private void print0(EConsoleColor color, String format, Object... args) {
		print0(color, String.format(format, args));
	}

	private void printAsync() {
		while (!Thread.currentThread().isInterrupted()) {
			String text = null;
			lock.lock();
			try {
				while (list.isEmpty()) {
					try {
						notEmpty.await();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt(); // Restore interrupted status
						return;
					}
				}

				text = list.remove(0);
			} finally {
				lock.unlock();
			}

			// Process outside the lock to allow concurrent additions
			if (text != null) {
				System.out.print(text);
			}
		}
	}
}
