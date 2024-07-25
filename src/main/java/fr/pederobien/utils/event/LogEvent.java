package fr.pederobien.utils.event;

public class LogEvent extends Event {
	
	public enum ELogLevel {
		INFO,
		DEBUG,
		WARNING,
		ERROR
	}
	private ELogLevel level;
	private String message;

	/**
	 * Creates a log event.
	 * 
	 * @param level The level of the log.
	 * @param format The formatter if the message to display has arguments.
	 * @param args The arguments of the message to display.
	 */
	public LogEvent(ELogLevel level, String format, Object... args) {
		this.level = level;
		this.message = String.format(format, args);
	}
	
	/**
	 * Creates a log event. The log level is INFO.
	 * 
	 * @param format The formatter if the message to display has arguments.
	 * @param args The arguments of the message to display.
	 */
	public LogEvent(String format, Object... args) {
		this(ELogLevel.INFO, format, args);
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
		return String.format("[%s] %s", level, message);
	}
}
