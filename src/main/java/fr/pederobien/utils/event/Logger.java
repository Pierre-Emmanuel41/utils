package fr.pederobien.utils.event;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import fr.pederobien.utils.BlockingQueueTask;

public final class Logger implements IEventListener {
	private static final BlockingQueueTask<String> CONSOLE;
	private static final EventCalledListener EVENT_LISTENER;
	private static boolean newLine, timeStamp, colorized;
	private static int debugLevel;

	static {
		CONSOLE = new BlockingQueueTask<String>("AsyncConsole", System.out::print);
		CONSOLE.start();

		EVENT_LISTENER = new EventCalledListener();

		newLine = true;
		timeStamp = true;
		colorized = false;
		debugLevel = -1;
	}

	/**
	 * Creates a LogEvent with log level INFO and the given formatted text.
	 * 
	 * @param format The formatter if the message to display has arguments.
	 * @param args   The arguments of the message to display.
	 */
	public static void info(String format, Object... args) {
		print(ELogType.INFO, format, args);
	}

	/**
	 * Creates a LogEvent with log level DEBUG and the given formatted text.
	 * 
	 * @param format The formatter if the message to display has arguments.
	 * @param args   The arguments of the message to display.
	 */
	public static void debug(int level, String format, Object... args) {
		if (debugLevel <= level)
			print(ELogType.DEBUG, format, args);
	}

	/**
	 * Creates a LogEvent with log level DEBUG and the given formatted text.
	 * 
	 * @param format The formatter if the message to display has arguments.
	 * @param args   The arguments of the message to display.
	 */
	public static void debug(String format, Object... args) {
		debug(0, format, args);
	}

	/**
	 * Creates a LogEvent with log level WARNING and the given formatted text.
	 * 
	 * @param format The formatter if the message to display has arguments.
	 * @param args   The arguments of the message to display.
	 */
	public static void warning(String format, Object... args) {
		print(ELogType.WARNING, format, args);
	}

	/**
	 * Creates a LogEvent with log level ERROR and the given formatted text.
	 * 
	 * @param format The formatter if the message to display has arguments.
	 * @param args   The arguments of the message to display.
	 */
	public static void error(String format, Object... args) {
		print(ELogType.ERROR, format, args);
	}

	/**
	 * Creates a LogEvent with log level NONE and the given formatted text.
	 * 
	 * @param format The formatter if the message to display has arguments.
	 * @param args   The arguments of the message to display.
	 */
	public static void print(String format, Object... args) {
		print(ELogType.NONE, format, args);
	}

	/**
	 * Encapsulate the given text with a timestamp and a new line depending on the timestamp and newline flag values.
	 * 
	 * @param format The formatter if the message to display has arguments.
	 * @param args   The arguments of the message to display.
	 */
	private static void print(ELogType logType, String format, Object... args) {
		String text = String.format(format, args);

		if (logType != ELogType.NONE) {
			text = String.format("[%s] %s", logType.name(), text);
			if (colorized)
				text = logType.getInColor(text);
		}

		if (timeStamp) {
			String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss:SSSS"));
			text = String.format("[%s] %s", time, text);
		}

		if (newLine)
			text = String.format("%s\n", text);

		CONSOLE.add(text);
	}

	/**
	 * Set if the logger shall print the event thrown by the event manager.
	 * 
	 * @param logEvent True to log events raised by the event manager, false otherwise.
	 */
	public static void setPrintEvent(boolean logEvent) {
		if (logEvent)
			EventManager.registerListener(EVENT_LISTENER);
		else
			EventManager.unregisterListener(EVENT_LISTENER);
	}

	/**
	 * Set if a new line should be displayed after displaying a thrown event.
	 * 
	 * @param newLine True in order to display a new line after, false otherwise.
	 */
	public static void setPrintNewLine(boolean newLine) {
		Logger.newLine = newLine;
	}

	/**
	 * Set if the time stamp should be displayed before a thrown event.
	 * 
	 * @param timeStamp True in order to display the time stamp, false otherwise.
	 */
	public static void setPrintTimeStamp(boolean timeStamp) {
		Logger.timeStamp = timeStamp;
	}

	/**
	 * Set the minimum level a debug log shall have to be print. The lower the value is, the lower in the application layers it is.
	 *
	 * @param debugLevel The minimum level.
	 */
	public static void setPrintDebugLevel(int debugLevel) {
		Logger.debugLevel = debugLevel;
	}

	/**
	 * Set if the logs shall be displayed in color depending on their type.
	 * 
	 * @param colorized True to display with color, false otherwise.
	 */
	public static void setPrintInColor(boolean colorized) {
		Logger.colorized = colorized;
	}

	/**
	 * Print the event in the console.
	 * 
	 * @param event The event to print.
	 */
	/*
	 * private void print(Event event) { String text = event.toString(); if (timeStamp) { String time =
	 * LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss:SSSS")); text = String.format("[%s] %s", time, text); }
	 * 
	 * if (newLine) text = String.format("%s\n", text);
	 * 
	 * queue.add(text); }
	 */

	public enum ELogType {
		// No color
		NONE("\u001B[0m"),

		// Red
		ERROR("\u001B[31m"),

		// Green
		EVENT("\u001B[32m"),

		// Yellow
		WARNING("\u001B[33m"),

		// Magenta
		INFO("\u001B[95m"),

		// Cyan
		DEBUG("\u001B[96m");

		private final String color;

		/**
		 * Creates a log type associated to a color.
		 * 
		 * @param color The color used to display the log message.
		 */
		ELogType(String color) {
			this.color = color;
		}

		/**
		 * Get a colored message base on the log type.
		 * 
		 * @param message The message to encapsulate in color.
		 * 
		 * @return The colored message.
		 */
		public String getInColor(String message) {
			return String.format("%s %s %s", color, message, NONE.color);
		}
	}

	private static class EventCalledListener implements IEventListener {

		@EventHandler(priority = EventPriority.LOWEST)
		private void onLog(EventCalledEvent event) {
			print(ELogType.EVENT, event.getEvent().toString());
		}
	}
}
