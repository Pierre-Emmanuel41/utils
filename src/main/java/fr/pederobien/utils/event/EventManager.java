package fr.pederobien.utils.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import fr.pederobien.utils.ICancellable;

public class EventManager {
	private static final Map<Class<? extends Event>, Map<EventPriority, BlockingQueue<Handler>>> HANDLERS;
	private static final Map<String, Map<Class<? extends Event>, BlockingQueue<Handler>>> LISTENERS;

	static {
		HANDLERS = new ConcurrentHashMap<Class<? extends Event>, Map<EventPriority, BlockingQueue<Handler>>>();
		LISTENERS = new ConcurrentHashMap<String, Map<Class<? extends Event>, BlockingQueue<Handler>>>();
	}

	/**
	 * Register the given event listener for events handling. There is no mechanism to check if the listener is already registered or
	 * not.
	 * 
	 * @param eventListener The listener that gather event handlers.
	 */
	public static void registerListener(IEventListener eventListener) {
		// Separating event listener into event handlers.
		Map<Class<? extends Event>, BlockingQueue<Handler>> newEventHandlers = createEventHandler(eventListener);
		LISTENERS.put(eventListener.getName(), newEventHandlers);

		// Registering event handler for specified event.
		for (Map.Entry<Class<? extends Event>, BlockingQueue<Handler>> entryEventHandlers : newEventHandlers.entrySet()) {
			Map<EventPriority, BlockingQueue<Handler>> eventHandlers = HANDLERS.get(entryEventHandlers.getKey());

			// Creating a new Map if there is no event handler registered for the event.
			if (eventHandlers == null) {
				eventHandlers = new ConcurrentHashMap<EventPriority, BlockingQueue<Handler>>();
				HANDLERS.put(entryEventHandlers.getKey(), eventHandlers);
			}

			for (Handler handler : entryEventHandlers.getValue()) {
				BlockingQueue<Handler> handlers = eventHandlers.get(handler.getPriority());

				// Creating a new list if there is no event list registered for the event priority.
				if (handlers == null) {
					handlers = new ArrayBlockingQueue<Handler>(10000);
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
		Map<Class<? extends Event>, BlockingQueue<Handler>> eventHandlers = LISTENERS.remove(eventListener.getName());

		// Listener not registered
		if (eventHandlers == null)
			return;

		for (Map.Entry<Class<? extends Event>, BlockingQueue<Handler>> entryHandler : eventHandlers.entrySet()) {
			Map<EventPriority, BlockingQueue<Handler>> handlersMap = HANDLERS.get(entryHandler.getKey());

			// No handlers registered for the given event.
			if (handlersMap == null)
				return;

			for (Handler handler : entryHandler.getValue()) {
				BlockingQueue<Handler> handlers = handlersMap.get(handler.getPriority());
				handlers.remove(handler);

				// Removing the handlers list because it does not contains any handlers.
				if (handlers.isEmpty())
					handlersMap.remove(handler.getPriority());
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
		doCall(new EventCalledEvent(event));
		doCall(event);
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

	private static Map<Class<? extends Event>, BlockingQueue<Handler>> createEventHandler(IEventListener eventListener) {
		Map<Class<? extends Event>, BlockingQueue<Handler>> eventHandlersMap = new HashMap<Class<? extends Event>, BlockingQueue<Handler>>();

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
			BlockingQueue<Handler> eventHandlerList = eventHandlersMap.get(eventClass);
			if (eventHandlerList == null) {
				eventHandlerList = new ArrayBlockingQueue<Handler>(10000);
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
		Map<EventPriority, BlockingQueue<Handler>> handlersMap = HANDLERS.get(event.getClass());

		// No handlers registered for the given event.
		if (handlersMap == null)
			return;

		Iterator<Map.Entry<EventPriority, BlockingQueue<Handler>>> handlerIterator = handlersMap.entrySet().iterator();
		while (handlerIterator.hasNext()) {
			Map.Entry<EventPriority, BlockingQueue<Handler>> entry = handlerIterator.next();
			Iterator<Handler> iterator = entry.getValue().iterator();
			while (iterator.hasNext()) {
				Handler handler = iterator.next();
				try {
					handler.handle(event);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
