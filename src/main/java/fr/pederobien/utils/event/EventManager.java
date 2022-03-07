package fr.pederobien.utils.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.function.Supplier;

import fr.pederobien.utils.ICancellable;

public class EventManager {
	private static final Map<Class<? extends Event>, Map<EventPriority, Queue<Handler>>> HANDLERS;
	private static final Map<String, Map<Class<? extends Event>, Queue<Handler>>> LISTENERS;

	static {
		HANDLERS = new ConcurrentHashMap<Class<? extends Event>, Map<EventPriority, Queue<Handler>>>();
		LISTENERS = new ConcurrentHashMap<String, Map<Class<? extends Event>, Queue<Handler>>>();
	}

	/**
	 * Register the given event listener for events handling. There is no mechanism to check if the listener is already registered or
	 * not.
	 * 
	 * @param eventListener The listener that gather event handlers.
	 */
	public static void registerListener(IEventListener eventListener) {
		// Separating event listener into event handlers.
		Map<Class<? extends Event>, Queue<Handler>> newEventHandlers = createEventHandler(eventListener);
		LISTENERS.put(getListenerName(eventListener), newEventHandlers);

		// Registering event handler for specified event.
		for (Map.Entry<Class<? extends Event>, Queue<Handler>> entryEventHandlers : newEventHandlers.entrySet()) {
			Map<EventPriority, Queue<Handler>> eventHandlers = HANDLERS.get(entryEventHandlers.getKey());

			// Creating a new Map if there is no event handler registered for the event.
			if (eventHandlers == null) {
				// Creating a map in order to associated the priority with an empty list.
				EnumMap<EventPriority, Queue<Handler>> enumMap = new EnumMap<EventPriority, Queue<Handler>>(EventPriority.class);
				for (EventPriority priority : EventPriority.values())
					enumMap.put(priority, new ConcurrentLinkedQueue<Handler>());

				// Registering the created map.
				eventHandlers = Collections.synchronizedMap(enumMap);
				HANDLERS.put(entryEventHandlers.getKey(), eventHandlers);
			}

			for (Handler handler : entryEventHandlers.getValue())
				eventHandlers.get(handler.getPriority()).add(handler);
		}
	}

	/**
	 * Unregister the given event listener.
	 * 
	 * @param eventListener The listener that gather event handlers.
	 */
	public static void unregisterListener(IEventListener eventListener) {
		Map<Class<? extends Event>, Queue<Handler>> eventHandlers = LISTENERS.remove(getListenerName(eventListener));

		// Listener not registered
		if (eventHandlers == null)
			return;

		for (Map.Entry<Class<? extends Event>, Queue<Handler>> entryHandler : eventHandlers.entrySet()) {
			Map<EventPriority, Queue<Handler>> handlersMap = HANDLERS.get(entryHandler.getKey());

			// No handlers registered for the given event.
			if (handlersMap == null)
				return;

			for (Handler handler : entryHandler.getValue()) {
				Queue<Handler> handlers = handlersMap.get(handler.getPriority());
				handlers.remove(handler);
			}
		}
	}

	/**
	 * Fire an {@link EventCallEvent} first, then fire the given event and dispatch it among the event handlers. It is recommended
	 * that each event overrides the toString method in order to display the value of each parameter.
	 * 
	 * @param event The event to fire.
	 */
	public static void callEvent(Event event) {
		doCall(event);
		doCall(new EventCalledEvent(event));
	}

	/**
	 * Fire the event among the event handlers and run the given runnable if the given event class does not implements
	 * {@link ICancellable} interface of if the event has not been cancelled.
	 * 
	 * @param event    The event to fire.
	 * @param runnable The code to run if the event is not cancelled.
	 */
	public static void callEvent(Event event, Runnable runnable) {
		callEvent(event);
		if (!(event instanceof ICancellable) || !((ICancellable) event).isCancelled())
			runnable.run();
	}

	/**
	 * First fire the preEvent among the event, then fire the postEvent if the given <code>preEvent</code> class does not implements
	 * {@link ICancellable} interface of if the event has not been cancelled.
	 * 
	 * @param preEvent The event to thrown first.
	 * @param posEvent The event to thrown at the end.
	 */
	public static void callEvent(Event preEvent, Event posEvent) {
		callEvent(preEvent, () -> callEvent(posEvent));
	}

	/**
	 * First fire the preEvent among the event, then run the given exe if the given event class does not implements
	 * {@link ICancellable} interface of if the event has not been cancelled and finally fire the postEvent.
	 * 
	 * @param preEvent The event to thrown first.
	 * @param exe      The code to execute if the event has not been cancelled.
	 * @param posEvent The event to thrown at the end.
	 */
	public static void callEvent(Event preEvent, Runnable exe, Event posEvent) {
		callEvent(preEvent);
		if (!(preEvent instanceof ICancellable) || !((ICancellable) preEvent).isCancelled()) {
			exe.run();
			callEvent(posEvent);
		}
	}

	/**
	 * First fire the preEvent among the event, then run the given exe if the given event class does not implements
	 * {@link ICancellable} interface of if the event has not been cancelled and finally fire the postEvent.
	 * 
	 * @param preEvent The event to thrown first.
	 * @param exe      The code to execute if the event has not been cancelled and specify if the post event should be thrown or not.
	 * @param posEvent The event to thrown at the end.
	 */
	public static void callEvent(Event preEvent, Supplier<Boolean> exe, Event posEvent) {
		callEvent(preEvent);
		if (!(preEvent instanceof ICancellable) || !((ICancellable) preEvent).isCancelled()) {
			if (exe.get())
				callEvent(posEvent);
		}
	}

	/**
	 * First fire the preEvent among the event, then run the given exe if the given event class does not implements
	 * {@link ICancellable} interface of if the event has not been cancelled and finally fire the postEvent.
	 * 
	 * @param preEvent The event to thrown first.
	 * @param exe      The code to execute if the event has not been cancelled and specify the type of the created object.
	 * @param posEvent A function to create the postEvent depending on the created object.
	 */
	public static <T> T callEvent(Event preEvent, Supplier<T> exe, Function<T, Event> postEvent) {
		callEvent(preEvent);
		if (!(preEvent instanceof ICancellable) || !((ICancellable) preEvent).isCancelled()) {
			T result = exe.get();
			callEvent(postEvent.apply(result));
			return result;
		}
		return null;
	}

	private static Map<Class<? extends Event>, Queue<Handler>> createEventHandler(IEventListener eventListener) {
		Map<Class<? extends Event>, Queue<Handler>> eventHandlersMap = new HashMap<Class<? extends Event>, Queue<Handler>>();

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
				String message = String.format("%s attempt to register an invalid event handler method signature %s", eventListener.getListenerName(),
						method.toGenericString());
				throw new EventRegistrationException(message);
			}

			// Get or create the list of event handler registered for the event.
			Class<? extends Event> eventClass = checkClass.asSubclass(Event.class);
			method.setAccessible(true);
			Queue<Handler> eventHandlerList = eventHandlersMap.get(eventClass);
			if (eventHandlerList == null) {
				eventHandlerList = new ConcurrentLinkedQueue<>();
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

	private static void doCall(Event event) {
		Map<EventPriority, Queue<Handler>> handlersMap = HANDLERS.get(event.getClass());

		// No handlers registered for the given event.
		if (handlersMap == null)
			return;

		Iterator<Map.Entry<EventPriority, Queue<Handler>>> handlerIterator = handlersMap.entrySet().iterator();
		while (handlerIterator.hasNext()) {
			Map.Entry<EventPriority, Queue<Handler>> entry = handlerIterator.next();
			for (Handler handler : entry.getValue()) {
				try {
					handler.handle(event);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Creates a unique listener name for the given listener.
	 * 
	 * @param listener The listener used to create a unique name.
	 * 
	 * @return The following string : <code>&lt;listenerName&gt;@&lt;hashcode&gt;</code>
	 */
	private static String getListenerName(IEventListener listener) {
		return String.format("%s@%s", listener.getListenerName(), listener.hashCode());
	}
}
