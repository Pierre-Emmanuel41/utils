package fr.pederobien.utils;

import java.util.concurrent.Semaphore;

public class HealedCounter {
	private int max;
	private int counter;
	private int time;
	private Runnable action;
	private String name;
	private Thread watcher;
	private Semaphore semaphore;

	/**
	 * Asynchronously monitor an underlying counter which can be incremented when
	 * the function {@link #increment()} is called and is decremented automatically
	 * after a specific period of time. A thread is looping trying to decrease the
	 * counter. If the counter reach 0, the thread sleep until the counter is
	 * incremented.
	 * 
	 * @param max    The maximum value the counter can reach.
	 * @param time   The time after which the counter is decremented.
	 * @param action The action to run when the counter reach the maximum value.
	 * @param name   The name of the watcher thread.
	 */
	public HealedCounter(int max, int time, Runnable action, String name) {
		this.max = max;
		this.time = time;
		this.action = action;
		this.name = name;

		initialize();
	}

	/**
	 * Asynchronously monitor an underlying counter which can be incremented when
	 * the function {@link #increment()} is called and is decremented automatically
	 * after a specific period of time. A thread is looping trying to decrease the
	 * counter. If the counter reach 0, the thread sleep until the counter is
	 * incremented.
	 * 
	 * @param max    The maximum value the counter can reach.
	 * @param time   The time after which the counter is decremented.
	 * @param action The action to run when the counter reach the maximum value.
	 */
	public HealedCounter(int max, int time, Runnable action) {
		this(max, time, action, "HealedCounter");
	}

	/**
	 * Increment the underlying counter if the maximum has not yet been reached.
	 * 
	 * @return true if the maximum value has been reached, false otherwise.
	 */
	public boolean increment() {
		int copy;

		// Incrementing the counter and use a copy for comparison
		synchronized (semaphore) {
			if (counter != max) {
				counter++;
			}
			copy = counter;
		}

		if (copy == max) {
			// Thread interruption will cause the thread to run the action
			watcher.interrupt();
			return true;
		}

		// Notifying the watcher thread to wake up to decrement the counter
		if (copy == 1)
			semaphore.release();

		return false;
	}

	/**
	 * @return The current counter value.
	 */
	public int get() {
		return counter;
	}

	/**
	 * Interrupt the thread which is trying to decrement the underlying counter, set
	 * the value of the counter to 0 and restart the watcher thread.
	 */
	public void reset() {
		watcher.interrupt();
		initialize();
	}

	/**
	 * Dispose the underlying thread that decrements the counter.
	 */
	public void dispose() {
		watcher.interrupt();
	}

	/**
	 * Set the internal counter to 0, instantiate watcher and semaphore.
	 */
	private void initialize() {
		counter = 0;
		watcher = new Thread(() -> watch(), name);
		semaphore = new Semaphore(0);

		watcher.setDaemon(true);
		watcher.start();
	}

	/**
	 * Asynchronous loop to decrement the counter.
	 */
	private void watch() {
		try {
			while (!watcher.isInterrupted()) {

				// If the counter value reached 0, the thread should sleep
				boolean wait;
				synchronized (semaphore) {
					wait = counter == 0;
				}

				if (wait) {
					// Waiting until the counter is incremented again
					semaphore.acquire();
					semaphore.drainPermits();
				}

				Thread.sleep(time);

				// Decrementing counter
				synchronized (semaphore) {
					counter--;
				}
			}
		} catch (InterruptedException e) {
			if (counter == max)
				action.run();
		}
	}
}
