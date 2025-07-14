package fr.pederobien.utils;

public interface ICancellable {

	/**
	 * Gets the cancellation state of this event. A cancelled event will not be
	 * executed in the server, but will still pass to other plugins
	 *
	 * @return true if this event is cancelled
	 */
	boolean isCancelled();

	/**
	 * Sets the cancellation state of this event. A cancelled event will not be
	 * executed in the server, but will still pass to other plugins.
	 *
	 * @param isCancelled true if you wish to cancel this event
	 */
	void setCancelled(boolean isCancelled);
}
