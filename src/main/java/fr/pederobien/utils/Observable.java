package fr.pederobien.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Observable<T> {
	private BlockingQueue<T> observers;

	public Observable() {
		observers = new ArrayBlockingQueue<T>(10000);
	}

	/**
	 * Append an observer to this observable object.
	 * 
	 * @param obs The observer to add.
	 */
	public void addObserver(T obs) {
		observers.add(obs);
	}

	/**
	 * Removes the given observers from the list of observer for this observable object.
	 * 
	 * @param obs The observer to remove.
	 */
	public void removeObserver(T obs) {
		observers.remove(obs);
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
		return Collections.unmodifiableList(new ArrayList<>(observers));
	}

	private void internalNotify(Stream<T> observers, Consumer<T> consumer) {
		observers.forEach(obj -> {
			try {
				consumer.accept(obj);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}
