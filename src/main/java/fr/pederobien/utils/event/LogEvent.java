package fr.pederobien.utils.event;

public class LogEvent extends Event {
	private static final String RESET = "\u001B[0m";
	private static final String RED = "\u001B[31m";
	private static final String YELLOW = "\u001B[33m";
	private static final String CYAN = "\u001B[96m";
	private static final String WHITE = "\u001B[37m";
	
	public enum ELogLevel {
		INFO(WHITE),
		DEBUG(CYAN),
		WARNING(YELLOW),
		ERROR(RED);
		
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
			return String.format("%s %s %s", color, message, RESET);
		}
	}
	private ELogLevel level;
	private String message;
	
	/**
	 * Creates a log event.
	 * 
	 * @param level The level of the log.
	 * @param colorise True to display a colored message, false otherwise.
	 * @param format The formatter if the message to display has arguments.
	 * @param args The arguments of the message to display.
	 */
	public LogEvent(ELogLevel level, boolean colorise, String format, Object... args) {
		this.level = level;
		
		String unformatted = String.format(format, args);
		if (!colorise)
			this.message = String.format("[%s] %s", level.name(), unformatted);
		else {
			String formatter = "[%s] %s";
			this.message = String.format(formatter, level.getInColor(level.name()), level.getInColor(unformatted));
		}
	}

	/**
	 * Creates a log event.
	 * 
	 * @param level The level of the log.
	 * @param format The formatter if the message to display has arguments.
	 * @param args The arguments of the message to display.
	 */
	public LogEvent(ELogLevel level, String format, Object... args) {
		this(level, true, format, args);
	}
	
	/**
	 * Creates a log event. The log level is INFO.
	 * 
	 * @param format The formatter if the message to display has arguments.
	 * @param args The arguments of the message to display.
	 */
	public LogEvent(boolean colorise, String format, Object... args) {
		this(ELogLevel.INFO, colorise, format, args);
	}
	
	/**
	 * Creates a log event. The log level is INFO.
	 * 
	 * @param format The formatter if the message to display has arguments.
	 * @param args The arguments of the message to display.
	 */
	public LogEvent(String format, Object... args) {
		this(false, format, args);
	}
	
	/**
	 * @return The log level of this event.
	 */
	public ELogLevel getLevel() {
		return level;
	}

	/**
	 * @return The log message of the event.
	 */
	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return message;
	}
}
