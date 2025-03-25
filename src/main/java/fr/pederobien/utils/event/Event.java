package fr.pederobien.utils.event;

public abstract class Event {

	/**
	 * Convenience method for providing a user-friendly identifier. By default, it
	 * is the event's class's {@linkplain Class#getSimpleName() simple name}.
	 *
	 * @return The name of this event.
	 */
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public String toString() {
		return getName();
	}
}
