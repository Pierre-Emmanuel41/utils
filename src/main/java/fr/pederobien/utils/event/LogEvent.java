package fr.pederobien.utils.event;

import java.util.StringJoiner;

public class LogEvent extends Event {
	private String message;

	public LogEvent(String format, Object... args) {
		this.message = String.format(format, args);
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("message=" + message);
		return String.format("%s_%s", getName(), joiner);
	}
}
