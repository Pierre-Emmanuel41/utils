package fr.pederobien.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Observable<T> {
	private List<T> observers, tempAddObserver, tempRemoveObserver;
	private boolean isNotifying, addObserverFlag, removeObserverFlag;

	public Observable() {
		observers = new ArrayList<T>();
		tempAddObserver = new ArrayList<T>();
		tempRemoveObserver = new ArrayList<T>();
	}

	/**
	 * Append an observer to this observable object.
	 * 
	 * @param obs The observer to add.
	 */
	public void addObserver(T obs) {
		if (!isNotifying) {
			observers.add(obs);
			return;
		}
		addObserverFlag = true;
		tempAddObserver.add(obs);
	}

	/**
	 * Removes the given observers from the list of observer for this observable object.
	 * 
	 * @param obs The observer to remove.
	 */
	public void removeObserver(T obs) {
		if (!isNotifying) {
			observers.remove(obs);
			return;
		}

		removeObserverFlag = true;
		tempRemoveObserver.add(obs);
	}

	/**
	 * Notify each observer that something has changed.
	 * 
	 * @param consumer The consumer used to know which method should be called on each observer.
	 */
	public void notifyObservers(Consumer<T> consumer) {
		if (size() > 0)
			internalNotify(observers.stream(), consumer);
	}

	/**
	 * Notify each observer that something has changed.
	 * 
	 * @param predicate A condition for each observer to be notified.
	 * @param consumer  The consumer used to know which method should be called on each observer.
	 */
	public void notifyObservers(Predicate<T> predicate, Consumer<T> consumer) {
		if (size() > 0)
			internalNotify(observers.stream().filter(predicate), consumer);
	}

	/**
	 * @return The number of observers for this observable object.
	 */
	public int size() {
		return observers.size();
	}

	/**
	 * @return A list that contains all registered observers for this observable. This list is unmodifiable.
	 */
	public List<T> getObservers() {
		return Collections.unmodifiableList(observers);
	}

	private void internalNotify(Stream<T> observers, Consumer<T> consumer) {
		isNotifying = true;
		observers.forEach(obj -> {
			try {
				consumer.accept(obj);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		if (addObserverFlag) {
			tempAddObserver.forEach(obs -> this.observers.add(obs));
			tempAddObserver.clear();
			addObserverFlag = false;
		}
		if (removeObserverFlag) {
			tempRemoveObserver.forEach(obs -> this.observers.remove(obs));
			tempRemoveObserver.clear();
			removeObserverFlag = false;
		}

		isNotifying = false;
	}
}
