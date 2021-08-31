package fr.pederobien.utils.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.pederobien.utils.ICancellable;

public class EventManager {
	private static final Map<Class<? extends Event>, Map<EventPriority, List<Handler>>> HANDLERS;
	private static final Map<String, Map<Class<? extends Event>, List<Handler>>> LISTENERS;

	static {
		HANDLERS = new HashMap<Class<? extends Event>, Map<EventPriority, List<Handler>>>();
		LISTENERS = new HashMap<String, Map<Class<? extends Event>, List<Handler>>>();
	}

	/**
	 * Register the given event listener for event.
	 * 
	 * @param eventListener The listener that gather event handlers.
	 */
	public static void registerListener(IEventListener eventListener) {
		// Separating event listener into event handlers.
		Map<Class<? extends Event>, List<Handler>> newEventHandlers = createEventHandler(eventListener);
		LISTENERS.put(eventListener.getName(), newEventHandlers);

		// Registering event handler for specified event.
		for (Map.Entry<Class<? extends Event>, List<Handler>> entryEventHandlers : newEventHandlers.entrySet()) {
			Map<EventPriority, List<Handler>> eventHandlers = HANDLERS.get(entryEventHandlers.getKey());

			// Creating a new Map if there is no event handler registered for the event.
			if (eventHandlers == null) {
				eventHandlers = new EnumMap<EventPriority, List<Handler>>(EventPriority.class);
				HANDLERS.put(entryEventHandlers.getKey(), eventHandlers);
			}

			for (Handler handler : entryEventHandlers.getValue()) {
				List<Handler> handlers = eventHandlers.get(handler.getPriority());

				// Creating a new list if there is no event list registered for the event priority.
				if (handlers == null) {
					handlers = new ArrayList<Handler>();
					eventHandlers.put(handler.getPriority(), handlers);
				}
				handlers.add(handler);
			}
		}
	}

	/**
	 * Unregister the given event listener.
	 * 
	 * @param eventListener The listener that gather event handlers.
	 */
	public static void unregisterListener(IEventListener eventListener) {
		Map<Class<? extends Event>, List<Handler>> eventHandlers = LISTENERS.remove(eventListener.getName());

		// Listener not registered
		if (eventHandlers == null)
			return;

		for (Map.Entry<Class<? extends Event>, List<Handler>> entryHandler : eventHandlers.entrySet()) {
			Map<EventPriority, List<Handler>> handlersMap = HANDLERS.get(entryHandler.getKey());

			// No handlers registered for the given event.
			if (handlersMap == null)
				return;

			for (Handler handler : entryHandler.getValue()) {
				List<Handler> handlers = handlersMap.get(handler.getPriority());
				handlers.remove(handler);

				// Removing the handlers list because it does not contains any handlers.
				if (handlers.isEmpty())
					handlersMap.remove(handler.getPriority());
			}
		}
	}

	/**
	 * Fire the given event and dispatch it among the event handlers.
	 * 
	 * @param event The event to fire.
	 */
	public static void callEvent(Event event) {
		Map<EventPriority, List<Handler>> handlersMap = HANDLERS.get(event.getClass());

		// No handlers registered for the given event.
		if (handlersMap == null)
			return;

		for (Map.Entry<EventPriority, List<Handler>> entryHandlers : handlersMap.entrySet()) {
			for (Handler handler : entryHandlers.getValue())
				try {
					handler.handle(event);
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}

	/**
	 * Fire the event among the event handlers and run the given runnable if and only if the event implements the {@link ICancellable}
	 * interface and the event is not cancelled.
	 * 
	 * @param event    The event to fire.
	 * @param runnable The code to run if the event is not cancelled.
	 */
	public static void callEvent(Event event, Runnable runnable) {
		callEvent(event);
		if (!(event instanceof ICancellable) || !((ICancellable) event).isCancelled()) {
			runnable.run();
		}
	}

	private static Map<Class<? extends Event>, List<Handler>> createEventHandler(IEventListener eventListener) {
		Map<Class<? extends Event>, List<Handler>> eventHandlersMap = new HashMap<Class<? extends Event>, List<Handler>>();

		List<Method> methods = new ArrayList<Method>();
		// private methods
		methods.addAll(Arrays.asList(eventListener.getClass().getDeclaredMethods()));

		// Iterating over public methods in order to extract event handlers.
		for (Method method : methods) {
			EventHandler eventHandler = method.getAnnotation(EventHandler.class);
			if (eventHandler == null)
				continue;

			// Do not register bridge or synthetic methods to avoid event duplication
			if (method.isBridge() || method.isSynthetic())
				continue;

			final Class<?> checkClass;
			if (method.getParameterTypes().length != 1 || !Event.class.isAssignableFrom(checkClass = method.getParameterTypes()[0])) {
				String message = String.format("%s attempt to register an invalid event handler method signature %s", eventListener.getName(), method.toGenericString());
				throw new EventRegistrationException(message);
			}

			// Get or create the list of event handler registered for the event.
			Class<? extends Event> eventClass = checkClass.asSubclass(Event.class);
			method.setAccessible(true);
			List<Handler> eventHandlerList = eventHandlersMap.get(eventClass);
			if (eventHandlerList == null) {
				eventHandlerList = new ArrayList<Handler>();
				eventHandlersMap.put(eventClass, eventHandlerList);
			}

			// Checking if event is deprecated.
			for (Class<?> clazz = eventClass; Event.class.isAssignableFrom(clazz); clazz = clazz.getSuperclass()) {
				if (clazz.getAnnotation(Deprecated.class) != null)
					throw new EventRegistrationException(String.format("%s is a deprecated event", eventClass.getSimpleName()));
			}

			eventHandlerList.add(new Handler(eventListener, eventHandler, method));
		}
		return eventHandlersMap;
	}
}
