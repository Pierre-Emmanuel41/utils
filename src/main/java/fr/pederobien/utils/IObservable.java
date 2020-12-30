package fr.pederobien.utils;

public interface IObservable<T> {

	/**
	 * Append an observer to this observable object.
	 * 
	 * @param obs The observer to add.
	 */
	void addObserver(T obs);

	/**
	 * Remove an observer from this observable object.
	 * 
	 * @param obs The observer to remove.
	 */
	void removeObserver(T obs);
}
