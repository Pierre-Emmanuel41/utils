package fr.pederobien.utils.event;

/**
 * Simple interface for tagging all event handler. EventHandler are methods annotated with {@link EventHandler}.
 */
public interface IEventListener {

	/**
	 * @return The name of this event listener.
	 */
	default String getListenerName() {
		return getClass().getSimpleName();
	}
}
