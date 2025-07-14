package fr.pederobien.utils.event;

import java.time.LocalDateTime;

public class EventCalledEvent extends Event {
	private final Event event;

	/**
	 * Creates an event thrown when an event has been registered in the
	 * {@link EventManager} in order to be called.
	 * 
	 * @param event The registered event to be called.
	 */
	public EventCalledEvent(Event event) {
		this.event = event;
	}

	/**
	 * @return The registered event to be called.
	 */
	public Event getEvent() {
		return event;
	}
}
