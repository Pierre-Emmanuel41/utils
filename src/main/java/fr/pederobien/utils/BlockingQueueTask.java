package fr.pederobien.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class BlockingQueueTask<T> {
	private Thread queueThread;
	private Consumer<T> consumer;
	private BlockingQueue<T> queue;
	private AtomicBoolean disposed;
	private boolean isStarted;

	/**
	 * Create a thread associated to a BlockingQueue.
	 * 
	 * @param name     The thread name.
	 * @param consumer The code to execute when an element is added to this queue.
	 */
	public BlockingQueueTask(String name, Consumer<T> consumer) {
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
	 */
	public void add(T e) {
		checkIsDisposed();
		queue.add(e);
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
				consumer.accept(queue.take());
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
}
