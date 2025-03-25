package fr.pederobien.utils.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import fr.pederobien.utils.ICancellable;

public class Handler {
	private IEventListener eventListener;
	private EventHandler eventHandler;
	private Method method;

	public Handler(IEventListener eventListener, EventHandler eventHandler, Method method) {
		this.eventListener = eventListener;
		this.eventHandler = eventHandler;
		this.method = method;
	}

	/**
	 * @return The priority of the underlying event handler.
	 */
	EventPriority getPriority() {
		return eventHandler.priority();
	}

	/**
	 * Handle the specified event by running the associated handler.
	 * 
	 * @param event The event to handle.
	 * 
	 * @exception IllegalAccessException      If this {@code Method} object is
	 *                                        enforcing Java language access control
	 *                                        and the underlying method is
	 *                                        inaccessible.
	 * @exception IllegalArgumentException    If the method is an instance method
	 *                                        and the specified object argument is
	 *                                        not an instance of the class or
	 *                                        interface declaring the underlying
	 *                                        method (or of a subclass or
	 *                                        implementor thereof); if the number of
	 *                                        actual and formal parameters differ;
	 *                                        if an unwrapping conversion for
	 *                                        primitive arguments fails; or if,
	 *                                        after possible unwrapping, a parameter
	 *                                        value cannot be converted to the
	 *                                        corresponding formal parameter type by
	 *                                        a method invocation conversion.
	 * @exception InvocationTargetException   If the underlying method throws an
	 *                                        exception.
	 * @exception NullPointerException        If the specified object is null and
	 *                                        the method is an instance method.
	 * @exception ExceptionInInitializerError If the initialization provoked by this
	 *                                        method fails.
	 */
	public void handle(Event event) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (event instanceof ICancellable)
			if (((ICancellable) event).isCancelled() && eventHandler.ignoreCancelled())
				return;

		method.invoke(eventListener, event);
	}
}
