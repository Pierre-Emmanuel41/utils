package fr.pederobien.utils.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.pederobien.utils.BlockingQueueTask;

public class EventLogger implements IEventListener {
	private static final String FORMATTER = "[%s] %s";
	private static final String NEW_LINE = "[%s] %s\r\n";
	private BlockingQueueTask<EventCalledEvent> task;
	private Set<Class<? extends Event>> ignored;
	private List<EventCalledEvent> events;
	private AtomicBoolean isRegistered;
	private String formatter;

	private EventLogger() {
		ignored = new HashSet<Class<? extends Event>>();
		events = Collections.synchronizedList(new ArrayList<>());
		isRegistered = new AtomicBoolean(false);
		formatter = NEW_LINE;
	}

	/**
	 * @return The singleton instance of this logger.
	 */
	public static EventLogger instance() {
		return SingletonHolder.LOGGER;
	}

	private static class SingletonHolder {
		private static final EventLogger LOGGER = new EventLogger();
	}

	/**
	 * Specifies a class of event that when called, should not be displayed by this logger.
	 * 
	 * @param clazz The class of event to not display.
	 */
	public <T extends Event> EventLogger ignore(Class<T> clazz) {
		if (ignored.contains(clazz))
			return this;
		ignored.add(clazz);
		return this;
	}

	/**
	 * Specifies a class of event that when called are not ignored any more.
	 * 
	 * @param clazz The class to not ignore.
	 */
	public void accept(Class<? super Event> clazz) {
		ignored.remove(clazz);
	}

	/**
	 * Register this listener in the EventManager in order to display the registered event to be called.
	 */
	public void register() {
		if (!isRegistered.compareAndSet(false, true))
			return;

		EventManager.registerListener(this);
		task = new BlockingQueueTask<>("EventLogger", event -> display(event));
		task.start();
	}

	/**
	 * Unregister this listener from the EventManager in order to not be notified when an event is thrown.
	 */
	public void unregister() {
		if (!isRegistered.compareAndSet(true, false))
			return;

		EventManager.unregisterListener(this);
		task.dispose();
	}

	/**
	 * @return The list that contains all events thrown while this logger was registered.
	 */
	public List<EventCalledEvent> getEvents() {
		return new ArrayList<EventCalledEvent>(events);
	}

	/**
	 * Set if a new line should be displayed after displaying an thrown event.
	 * 
	 * @param newLine True in order to display a new line after, false otherwise.
	 */
	public EventLogger displayNewLine(boolean newLine) {
		formatter = newLine ? NEW_LINE : FORMATTER;
		return this;
	}

	@EventHandler
	private void onLog(EventCalledEvent event) {
		task.add(event);
	}

	private void display(EventCalledEvent event) {
		events.add(event);
		if (ignored.contains(event.getClass()) || isSuperClassIgnored(event))
			return;

		System.out.print(String.format(formatter, event.getTime().toLocalTime(), event.getEvent()));
	}

	/**
	 * Check if a super class of the called event is forbidden.
	 * 
	 * @param event The event that contains the called event.
	 * @return True if a super class is forbidden, false otherwise.
	 */
	private boolean isSuperClassIgnored(EventCalledEvent event) {
		for (Class<?> clazz = event.getEvent().getClass(); Event.class.isAssignableFrom(clazz); clazz = clazz.getSuperclass())
			if (ignored.contains(clazz))
				return true;
		return false;
	}
}
