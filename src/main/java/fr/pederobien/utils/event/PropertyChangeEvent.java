package fr.pederobien.utils.event;

public class PropertyChangeEvent<T> extends Event {
	private T oldValue, newValue;

	/**
	 * Creates an event thrown when the value of a property has changed.
	 * 
	 * @param oldValue The old value of the property.
	 * @param newValue The new value of the property.
	 */
	public PropertyChangeEvent(T oldValue, T newValue) {
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	/**
	 * @return The old value associated to the property.
	 */
	public T getOldValue() {
		return oldValue;
	}

	/**
	 * @return The new value of the property.
	 */
	public T getNewValue() {
		return newValue;
	}
}
