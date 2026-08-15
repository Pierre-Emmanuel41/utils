package fr.pederobien.utils;

public class Logger {

	private static class SingletonHolder {
		private static final Logger LOGGER;

		static {
			LOGGER = new Logger();
		}
	}

	/**
	 * Set if a new line shall be printed after printing a text.
	 * 
	 * @param newLine True to print a new line, false otherwise.
	 */
	public static final void setNewLine(boolean newLine) {
		AsyncConsole.setNewLine(newLine);
	}

	/**
	 * Set if the time stamp shall be printed before printing a text.
	 * 
	 * @param timeStamp True to print time stamp, false otherwise.
	 */
	public static final void setTimeStamp(boolean timestamp) {
		AsyncConsole.setTimeStamp(timestamp);
	}

	/**
	 * Set if the color shall be used when printing a text.
	 * 
	 * @param colorized True to use the color, false otherwise.
	 */
	public static final void setColorized(boolean colorized) {
		AsyncConsole.setColorized(colorized);
	}

	/**
	 * Set the minimum level a debug log shall have to be print. The lower the value
	 * is, the lower in the application layers it is.
	 *
	 * @param level The minimum level.
	 */
	public static final void setLevel(int level) {
		get().setLevel0(level);
	}

	/**
	 * Prints a log asynchronously using the AsyncConsole and the given color.
	 * 
	 * @param color  The color to use to print the input text.
	 * @param format The formatter if the message to display has arguments.
	 * @param args   The arguments of the message to display.
	 */
	public static void print(EConsoleColor color, String format, Object... args) {
		get().print0(color, format, args);
	}

	/**
	 * Prints an INFORMATION log asynchronously using the AsyncConsole. The color
	 * used for printing is MAGENTA.
	 * 
	 * @param format The formatter if the message to display has arguments.
	 * @param args   The arguments of the message to display.
	 */
	public static void info(String format, Object... args) {
		print(EConsoleColor.MAGENTA, "[INFO] - %s", String.format(format, args));
	}

	/**
	 * Prints an DEBUG log asynchronously using the AsyncConsole. The color used for
	 * printing is CYAN.
	 * 
	 * @param format The formatter if the message to display has arguments.
	 * @param args   The arguments of the message to display.
	 */
	public static void debug(int level, String format, Object... args) {
		get().debug0(level, format, args);
	}

	/**
	 * Prints an DEBUG log asynchronously using the AsyncConsole. The color used for
	 * printing is CYAN. The debug level is 0.
	 * 
	 * @param format The formatter if the message to display has arguments.
	 * @param args   The arguments of the message to display.
	 */
	public static void debug(String format, Object... args) {
		get().debug0(0, format, args);
	}

	/**
	 * Prints an WARNING log asynchronously using the AsyncConsole. The color used
	 * for printing is YELLOW.
	 * 
	 * @param format The formatter if the message to display has arguments.
	 * @param args   The arguments of the message to display.
	 */
	public static void warning(String format, Object... args) {
		print(EConsoleColor.YELLOW, "[WARNING] - %s", String.format(format, args));
	}

	/**
	 * Prints an ERROR log asynchronously using the AsyncConsole. The color used for
	 * printing is RED.
	 * 
	 * @param format The formatter if the message to display has arguments.
	 * @param args   The arguments of the message to display.
	 */
	public static void error(String format, Object... args) {
		print(EConsoleColor.RED, "[ERROR] - %s", String.format(format, args));
	}

	/**
	 * Creates a LogEvent with log level NONE and the given formatted text.
	 * 
	 * @param format The formatter if the message to display has arguments.
	 * @param args   The arguments of the message to display.
	 */
	public static void print(String format, Object... args) {
		print(EConsoleColor.RESET, format, args);
	}

	private static Logger get() {
		return SingletonHolder.LOGGER;
	}

	private int level;

	private Logger() {
		level = -1;
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
		AsyncConsole.print(color, format, args);
	}

	/**
	 * Check if the given level is higher than the debug level, if so then format
	 * the given format and args array before printing the result asynchronously.
	 * 
	 * @param level  The level of the debug log.
	 * @param format The string format.
	 * @param args   The format's arguments.
	 */
	private void debug0(int level, String format, Object... args) {
		if (this.level < level)
			print0(EConsoleColor.CYAN, "[DEBUG] - %s", String.format(format, args));
	}

	/**
	 * Set the minimum level a debug log shall have to be print. The lower the value
	 * is, the lower in the application layers it is.
	 *
	 * @param level The minimum level.
	 */
	private void setLevel0(int level) {
		this.level = level;
	}
}
