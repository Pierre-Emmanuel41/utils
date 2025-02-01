package fr.pederobien.utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Watchdog {

	/**
	 * Execute asynchronously the runnable and block until the end of the execution or until a timeout occurs.
	 * 
	 * @param executable The code to execute.
	 * @param timeout The timeout in ms.
	 * 
	 * @return True if no timeout occurred, false otherwise.
	 */
	public static boolean start(IExecutable executable, int timeout) throws InterruptedException {
		return new WatchdogStakeholder(executable, timeout).start();
	}

	/**
	 * Create a watchdog stakeholder to execute asynchronously the runnable and
	 * block until the end of the execution or for a timeout.
	 * 
	 * @param executable The code to execute.
	 * @param timeout The timeout in ms.
	 * 
	 * @return The watchdog to execute the monitored task.
	 */
	public static WatchdogStakeholder create(IExecutable executable, int timeout) {
		return new WatchdogStakeholder(executable, timeout);
	}

	public static class WatchdogStakeholder {
		private IExecutable executable;
		private int timeout;
		private Thread worker;
		private Thread watcher;
		private Semaphore out, monitor;
		private CountDownLatch countdownLatch;
		private boolean cancelled, exception, success;

		/**
		 * Creates a watchdog to execute code asynchronously within a limited time.
		 * 
		 * @param executable The code to execute.
		 * @param timeout The time, in ms, after which a timeout occurs.
		 */
		private WatchdogStakeholder(IExecutable executable, int timeout) {
			this.executable = executable;
			this.timeout = timeout;

			watcher = new Thread(() -> watch(), "Watcher");
			out = new Semaphore(0);
			monitor = new Semaphore(1);
			countdownLatch = new CountDownLatch(2);

			cancelled = false;
			exception = false;
			success = false;
		}

		/**
		 * Cancel the execution of the task.
		 */
		public void cancel() {
			cancelled = true;

			// Notifying the watcher thread
			monitor.release();
		}

		/**
		 * Start the execution the of task in a separated thread and block until the end of it execution.
		 * 
		 * @return True if the task execution ended in time, false if a timeout occurred or if the task was cancelled.
		 */
		public boolean start() throws InterruptedException {
			cancelled = false;
			exception = false;

			watcher.setDaemon(true);
			watcher.start();

			// Wait for the worker or the watcher
			out.acquire();

			return success;
		}

		private void work() {
			try {
				// Acquiring permit to start timing monitoring
				monitor.acquire();

				// Waiting a little bit to be sure the watcher thread is ready to monitor
				countdownLatch.countDown();
				countdownLatch.await();

				// Execute the source code
				executable.exec();
			} catch (Exception e) {
				exception = true;
			} finally {
				// End of the execution
				monitor.release();
			}
		}

		private void watch() {
			boolean success = false;
			try {
				// Watcher is ready
				worker = new Thread(() -> work(), "Worker");
				worker.start();

				// Waiting a little bit to be sure the worker thread acquired the monitor semaphore
				countdownLatch.countDown();
				countdownLatch.await();

				boolean inTime = monitor.tryAcquire(timeout, TimeUnit.MILLISECONDS);

				// When the execution is cancelled, inTime will be true as the monitor is released
				success = (cancelled || exception || !inTime) ? false : true;

			} catch (InterruptedException e) {
				success = false;
			}

			this.success = success;
			if (!success)
				worker.interrupt();

			out.release();
		}
	}
}
