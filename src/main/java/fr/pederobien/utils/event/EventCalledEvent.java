package fr.pederobien.utils.event;

import java.time.LocalDateTime;

public class EventCalledEvent extends Event {
	private Event event;
	private LocalDateTime time;

	/**
	 * Creates an event thrown when an event has been registered in the
	 * {@link EventManager} in order to be called.
	 * 
	 * @param event The registered event to be called.
	 */
	public EventCalledEvent(Event event) {
		this.event = event;
		time = LocalDateTime.now();
	}

	/**
	 * @return The registered event to be called.
	 */
	public Event getEvent() {
		return event;
	}

	/**
	 * @return The time at which the called event has been thrown.
	 */
	public LocalDateTime getTime() {
		return time;
	}
}
