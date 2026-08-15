package fr.pederobien.utils;

public enum EConsoleColor {

	/**
	 * Use default console color.
	 */
	RESET("\u001B[0m"),

	/**
	 * Black color.
	 */
	BLACK("\u001B[30m"),

	/**
	 * Red color.
	 */
	RED("\u001B[31m"),

	/**
	 * Green color.
	 */
	GREEN("\u001B[32m"),

	/**
	 * Yellow color.
	 */
	YELLOW("\u001B[33m"),

	/**
	 * Blue color.
	 */
	BLUE("\u001B[34m"),

	/**
	 * Magenta color.
	 */
	MAGENTA("\u001B[35m"),

	/**
	 * Cyan color.
	 */
	CYAN("\u001B[36m"),

	/**
	 * White color.
	 */
	WHITE("\u001B[37m");

	private String color;

	private EConsoleColor(String color) {
		this.color = color;
	}

	/**
	 * Update the input text font color.
	 * 
	 * @param text The input text to update.
	 * @return The input text but with this font color.
	 */
	public String color(String text) {
		return String.format("%s%s%s", color, text, RESET.color);
	}
}
