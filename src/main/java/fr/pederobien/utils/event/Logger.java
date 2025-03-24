package fr.pederobien.utils.event;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.pederobien.utils.BlockingQueueTask;

public class Logger implements IEventListener {
	private BlockingQueueTask<String> queue;
	private Set<Class<? extends Event>> ignored;
	private AtomicBoolean isRegistered;
	private boolean newLine, timeStamp;

	private Logger() {
		ignored = new HashSet<Class<? extends Event>>();
		isRegistered = new AtomicBoolean(false);

		queue = new BlockingQueueTask<String>("AsyncConsole", text -> System.out.print(text));
		queue.start();
	}

	/**
	 * Creates a LogEvent with log level INFO and the given formatted text.
	 * 
	 * @param format The formatter if the message to display has arguments.
	 * @param args   The arguments of the message to display.
	 */
	public static void info(String format, Object... args) {
		instance().print(new LogEvent(ELogLevel.INFO, format, args));
	}

	/**
	 * Creates a LogEvent with log level DEBUG and the given formatted text.
	 * 
	 * @param format The formatter if the message to display has arguments.
	 * @param args   The arguments of the message to display.
	 */
	public static void debug(String format, Object... args) {
		instance().print(new LogEvent(ELogLevel.DEBUG, format, args));
	}

	/**
	 * Creates a LogEvent with log level WARNING and the given formatted text.
	 * 
	 * @param format The formatter if the message to display has arguments.
	 * @param args   The arguments of the message to display.
	 */
	public static void warning(String format, Object... args) {
		instance().print(new LogEvent(ELogLevel.WARNING, format, args));
	}

	/**
	 * Creates a LogEvent with log level ERROR and the given formatted text.
	 * 
	 * @param format The formatter if the message to display has arguments.
	 * @param args   The arguments of the message to display.
	 */
	public static void error(String format, Object... args) {
		instance().print(new LogEvent(ELogLevel.ERROR, format, args));
	}

	/**
	 * Creates a LogEvent with log level NONE and the given formatted text.
	 * 
	 * @param format The formatter if the message to display has arguments.
	 * @param args   The arguments of the message to display.
	 */
	public static void print(String format, Object... args) {
		instance().print(new LogEvent(ELogLevel.NONE, format, args));
	}

	/**
	 * @return The singleton instance of this logger.
	 */
	public static Logger instance() {
		return SingletonHolder.LOGGER;
	}

	private static class SingletonHolder {
		private static final Logger LOGGER = new Logger();
	}

	/**
	 * Specifies a class of event that when called, should not be displayed by this
	 * logger.
	 * 
	 * @param clazz The class of event to not display.
	 */
	public <T extends Event> Logger ignore(Class<T> clazz) {
		if (ignored.contains(clazz))
			return this;
		ignored.add(clazz);
		return this;
	}

	/**
	 * Specifies a class of event that when called are not ignored any more.
	 * 
	 * @param clazz The class to not ignore.
	 */
	public <T extends Event> void accept(Class<T> clazz) {
		ignored.remove(clazz);
	}

	/**
	 * Register this listener in the EventManager in order to display the registered
	 * event to be called.
	 */
	public void register() {
		if (!isRegistered.compareAndSet(false, true))
			return;

		EventManager.registerListener(this);
	}

	/**
	 * Unregister this listener from the EventManager in order to not be notified
	 * when an event is thrown.
	 */
	public void unregister() {
		if (!isRegistered.compareAndSet(true, false))
			return;

		EventManager.unregisterListener(this);
	}

	/**
	 * Set if a new line should be displayed after displaying a thrown event.
	 * 
	 * @param newLine True in order to display a new line after, false otherwise.
	 * 
	 * @return This logger.
	 */
	public Logger newLine(boolean newLine) {
		this.newLine = newLine;
		return this;
	}

	/**
	 * Set if a the time stamp should be displayed before a thrown event.
	 * 
	 * @param timeStamp True in order to display the time stamp, false otherwise.
	 * 
	 * @return This logger.
	 */
	public Logger timeStamp(boolean timeStamp) {
		this.timeStamp = timeStamp;
		return this;
	}

	/**
	 * Set if the logs shall be displayed in color depending on their level.
	 * 
	 * @param colorized True to display with color, false otherwise.
	 * 
	 * @return This logger.
	 */
	public Logger colorized(boolean colorized) {
		LogEvent.colorized = colorized;
		return this;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onLog(EventCalledEvent event) {
		if (ignored.contains(event.getClass()) || isSuperClassIgnored(event))
			return;

		print(event.getEvent());
	}

	/**
	 * Print the event in the console.
	 * 
	 * @param event The event to print.
	 */
	private void print(Event event) {
		String text = event.toString();
		if (timeStamp) {
			String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss:SSSS"));
			text = String.format("[%s] %s", time, text);
		}

		if (newLine)
			text = String.format("%s\n", text);

		queue.add(text);
	}

	/**
	 * Check if a super class of the called event is forbidden.
	 * 
	 * @param event The event that contains the called event.
	 * @return True if a super class is forbidden, false otherwise.
	 */
	private boolean isSuperClassIgnored(EventCalledEvent event) {
		for (Class<?> clazz = event.getEvent().getClass(); Event.class
				.isAssignableFrom(clazz); clazz = clazz.getSuperclass())
			if (ignored.contains(clazz))
				return true;
		return false;
	}

	public enum ELogLevel {

		// No color
		NONE("\u001B[0m"),

		// Magenta
		INFO("\u001B[95m"),

		// Cyan
		DEBUG("\u001B[96m"),

		// Yellow
		WARNING("\u001B[33m"),

		// Red
		ERROR("\u001B[31m");

		private String color;

		/**
		 * Creates a log level associated to a color.
		 * 
		 * @param color The color used to display the log message.
		 */
		private ELogLevel(String color) {
			this.color = color;
		}

		/**
		 * Get a colored message base on the log level.
		 * 
		 * @param message The message to encapsulate in color.
		 * 
		 * @return The colored message.
		 */
		public String getInColor(String message) {
			return String.format("%s %s %s", color, message, NONE.color);
		}
	}

	private static class LogEvent extends Event {
		private static boolean colorized;
		private String message;

		/**
		 * Creates a log event.
		 * 
		 * @param level  The level of the log.
		 * @param format The formatter if the message to display has arguments.
		 * @param args   The arguments of the message to display.
		 */
		private LogEvent(ELogLevel level, String format, Object... args) {
			String raw = String.format(format, args);
			if (level == ELogLevel.NONE) {
				message = raw;
			} else {
				message = String.format("[%s] %s", level.name(), raw);
				if (colorized)
					message = level.getInColor(message);
			}
		}

		@Override
		public String toString() {
			return message;
		}
	}
}
