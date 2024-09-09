package fr.pederobien.utils;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Watchdog {
	
	/**
	 * Execute asynchronously the runnable and block until the end of the execution or for a timeout.
	 * 
	 * @param executable The code to execute.
	 * @param timeout The timeout in ms.
	 * 
	 * @return True if no timeout occurred, false otherwise.
	 * @throws InterruptedException 
	 */
	public static boolean execute(IExecutable executable, int timeout) throws InterruptedException {
		return new WatchdogStakeholder(executable, timeout).execute();
	}
	
	private static class WatchdogStakeholder {
		private IExecutable executable;
		private int timeout;
		private Thread worker;
		private Thread watcher;
		private AtomicBoolean success;
		private Semaphore out, monitor;
		
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
		}
		
		public boolean execute() throws InterruptedException {
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
				
				if (monitor.tryAcquire(timeout, TimeUnit.MILLISECONDS))
					setSuccess(true);
				else {
					// Stopping worker thread execution
					worker.interrupt();
					setSuccess(false);
				}
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
