package fr.pederobien.utils;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
		private AtomicBoolean success;
		private Semaphore out, monitor;
		private boolean cancelled;
		
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
			success = new AtomicBoolean(false);
			out = new Semaphore(0);
			monitor = new Semaphore(1);
			
			cancelled = false;
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
			if (cancelled)
				return false;

			watcher.setDaemon(true);
			watcher.start();
			
			// Wait for the worker or the watcher
			out.acquire();
			return success.get();
		}
		
		private void work() {
			try {
				// Acquiring permit to start timing monitoring
				monitor.acquire();
				
				// Execute the source code
				executable.exec();
				
				// End of the execution
				monitor.release();
			} catch (Exception e) {
				// Do nothing
			}
		}
		
		private void watch() {
			try {
				// Watcher is ready
				worker = new Thread(() -> work(), "Worker");
				worker.start();
				
				// Waiting a little bit to be sure the worker thread acquired the monitor semaphore
				Thread.sleep(100);
				
				boolean inTime = monitor.tryAcquire(timeout, TimeUnit.MILLISECONDS);
				
				// When the execution is cancelled, inTime will be true as the monitor is released
				if (cancelled || !inTime) {
					worker.interrupt();
					setSuccess(false);
				}
				else
					setSuccess(true);
			} catch (InterruptedException e) {
				worker.interrupt();
				setSuccess(false);
			} finally {
				monitor.release();
			}
		}
		
		private void setSuccess(boolean isSuccess) {
			success.set(isSuccess);
			out.release();
		}
	}
}
