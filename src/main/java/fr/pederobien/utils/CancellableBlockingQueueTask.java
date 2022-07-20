package fr.pederobien.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class CancellableBlockingQueueTask<T> {
	private Thread queueThread;
	private Consumer<Cancellable<T>> consumer;
	private BlockingQueue<Cancellable<T>> queue;
	private AtomicBoolean disposed;
	private boolean isStarted;

	/**
	 * Create a thread associated to a BlockingQueue.
	 * 
	 * @param name     The thread name.
	 * @param consumer The code to execute when an element is added to this queue.
	 */
	public CancellableBlockingQueueTask(String name, Consumer<Cancellable<T>> consumer) {
		this.consumer = consumer;

		queue = new ArrayBlockingQueue<>(10000);

		queueThread = new Thread(() -> internalStart(), name);
		queueThread.setDaemon(true);

		disposed = new AtomicBoolean(false);
	}

	/**
	 * Start the underlying thread in order to perform an action when an element is added.
	 */
	public void start() {
		checkIsDisposed();

		if (isStarted)
			return;

		queueThread.start();
		isStarted = true;
	}

	/**
	 * Appends the given element in the underlying blocking queue in order to perform an action asynchronously.
	 * 
	 * @param e The element to in add.
	 * 
	 * @return a cancellable that wrap the element to add.
	 */
	public Cancellable<T> add(T e) {
		checkIsDisposed();
		Cancellable<T> element = new Cancellable<T>(e);
		queue.add(element);
		return element;
	}

	/**
	 * Dispose this queue. The underlying thread is interrupted, this object is no more reusable.
	 */
	public void dispose() {
		if (!disposed.compareAndSet(false, true))
			return;

		queueThread.interrupt();
	}

	private boolean isDisposed() {
		return disposed.get();
	}

	private void internalStart() {
		while (!isDisposed()) {
			try {
				Cancellable<T> cancellable = queue.take();
				if (cancellable.isCancelled())
					continue;

				consumer.accept(cancellable);
			} catch (InterruptedException e) {

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void checkIsDisposed() {
		if (isDisposed())
			throw new UnsupportedOperationException("Object disposed");
	}

	public static class Cancellable<T> {
		private T element;
		private boolean isCancelled;

		/**
		 * Creates a cancellable element.
		 * 
		 * @param element The underlying element associated to this cancellable element.
		 */
		private Cancellable(T element) {
			this.element = element;
			this.isCancelled = false;
		}

		/**
		 * @return The underlying element of this cancellable element.
		 */
		public T get() {
			return element;
		}

		/**
		 * @return True if this element has been cancelled, false otherwise.
		 */
		public boolean isCancelled() {
			return isCancelled;
		}

		public void setCancelled(boolean isCancelled) {
			this.isCancelled = isCancelled;
		}
	}
}
