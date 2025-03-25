package fr.pederobien.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

public class BlockingQueueTask<T> {
	private Thread queueThread;
	private Consumer<T> consumer;
	private BlockingQueue<T> queue;
	private IDisposable disposable;
	private boolean isStarted;
	private Semaphore pause;

	/**
	 * Create a thread associated to a BlockingQueue.
	 * 
	 * @param name     The thread name.
	 * @param consumer The code to execute asynchronously when an element is added
	 *                 to this queue.
	 */
	public BlockingQueueTask(String name, Consumer<T> consumer) {
		this.consumer = consumer;

		queue = new ArrayBlockingQueue<>(10000);

		queueThread = new Thread(() -> internalStart(), name);
		queueThread.setDaemon(true);

		disposable = new Disposable();
		pause = new Semaphore(1, true);
	}

	/**
	 * Start the underlying thread in order to perform an action when an element is
	 * added.
	 */
	public void start() {
		disposable.checkDisposed();

		if (isStarted)
			return;

		queueThread.start();
		isStarted = true;
	}

	/**
	 * Appends the given element in the underlying blocking queue in order to
	 * perform an action asynchronously.
	 * 
	 * @param e The element to add.
	 */
	public void add(T e) {
		disposable.checkDisposed();
		queue.add(e);
	}

	/**
	 * Force the underlying thread to sleep until the method resume is called.
	 */
	public void pause() throws InterruptedException {
		pause.acquire();
	}

	/**
	 * Resume the execution of the underlying thread.
	 */
	public void resume() {
		pause.release();
	}

	/**
	 * Dispose this queue. The underlying thread is interrupted, this object is no
	 * more reusable.
	 */
	public void dispose() {
		if (disposable.dispose())
			queueThread.interrupt();
	}

	private boolean isDisposed() {
		return disposable.isDisposed();
	}

	private void internalStart() {
		while (!isDisposed()) {
			try {
				// If function pause has been called,
				// the thread will wait until function resume is called
				pause.acquire();
				consumer.accept(queue.take());
				pause.release();
			} catch (InterruptedException e) {
				// Queue has been disposed, nothing to do
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
